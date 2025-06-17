package customer;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.its.Batcher;
import com.its.ExceptionEx;
import com.its.ListEx;
import com.its.ResultSetEx;
import com.its.insurancenow.INowEntityLoader;
import com.its.insurancenow.INowRelayRequest;
import com.its.insurancenow.INowRelayResult;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PolicyLoader implements INowEntityLoader
{
	/* Round trips to the database server are SLOW, so querying for multiple rows
	 * at once is a big performance win. The sort order ensures that we will always
	 * see the most relevant version of the policy first, so we can safely ignore
	 * any later rows we see with that policy identifier */
	private static final String QUERY =
		"select * from PMSP0200 where concat(concat(TRIM(SYMBOL),'-'),POLICY0NUM) in (%s) order by MODULE desc";

	/* How many policies to request in a single query */
	private static final int POLICIES_PER_QUERY = 50;

	private final Logger logger;


	@Inject
	public PolicyLoader(Logger logger)
	{
		this.logger = logger;
	}


	/**
	 * Return an entity identifier in our canonical customer-facing format from the
	 * provided database row.
	 */
	private String extractCustomerIdentifier(Map<String, Object> row)
	{
		var symbol = requireNonNull((String)row.get("SYMBOL")).trim();
		var policy = requireNonNull((String)row.get("POLICY0NUM"));
		return String.join("-", symbol, policy);
	}


	private void loadBatch(List<RelayWorkItem> workItems)
	{
		this.logger.info("Connecting to database on {}", Thread.currentThread());

		try (var connection = workItems.get(0).dataSource().getConnection())
		{
			/* Add a "?" placeholder to the SQL select statement for each identifier
			 * we want to include in the query. Batching them up this way helps offset
			 * the slow round trip call to the database server */

			var placeholders = "?" + ",?".repeat(workItems.size() - 1);
			var query = String.format(QUERY, placeholders);
			this.logger.debug("`{}`", query);

			try (var statement = connection.prepareStatement(query))
			{
				/* Match up the entity identifiers from our list of work items with the
				 * placeholders we placed into the query */

				for (var i = 0; i < workItems.size(); ++i)
					statement.setString(i + 1, workItems.get(i).identifier());

				/* Now we can execute the query and process the results. The call to `distinct()`
				 * ensures that we only get the most recent module for each policy, since the
				 * query sorts on that column in descending order */

				ResultSetEx.of(statement.executeQuery())
					.stream()
					.distinct(this::extractCustomerIdentifier)
					.forEach(row -> {

						var identifier = this.extractCustomerIdentifier(row);
						var module = requireNonNull((String)row.get("MODULE"));
						this.logger.info("Loading {} ({})", identifier, module);

						var workItem = ListEx.findFirst(workItems, item ->
							identifier.equals(item.identifier()))
							.orElseThrow();

						// HERE - PROCESS THE ROW, GET A RESULT

						workItem.complete();
						this.logger.info("Completed {} ({})", identifier, module);
					});
			}
		}
		catch (SQLException e)
		{
			throw ExceptionEx.toUnchecked(e);
		}
	}


	/**
	 * Loader entry point, called by {@see com.its.insurancenow.INowRelayService}
	 * when a load request is received.
	 */
	public void process(INowRelayRequest request, Consumer<INowRelayResult> onComplete, Stream<String> identifiers)
	{
		/* Set up a database connection  pool to improve performance. This will obviously need to
		 * be configured somewhere else at some point. */
		var config = new HikariConfig();
		config.setJdbcUrl("jdbc:as400://10.100.1.250/ITSDBSTAGE");
		config.setDriverClassName("com.ibm.as400.access.AS400JDBCDriver");
		config.setUsername("ITSUSER");
		config.setPassword(System.getenv("RELAY_STAGE")); // <--- *****************!!

		try (var dataSource = new HikariDataSource(config))
		{
			/* Set up a virtual thread pool to run queries in parallel */
			try (var taskScope = new StructuredTaskScope<Boolean>())
			{
				/* Batch up identifiers so I can query for a bunch of them all at once, helping
				 * to offset the slow database roundtrip times */

				var workItems = new Batcher<RelayWorkItem>(POLICIES_PER_QUERY, batch ->
					taskScope.fork(() -> {
						this.loadBatch(batch);
						return true;
					}));

				/* Wrap each identifier in a relay work item object, which associates all of
				 * the contextual information needed to do complete the load, and then push
				 * those work items into the batcher for processing */

				identifiers
					.map(identifier -> RelayWorkItem.builder()
						.request(request)
						.identifier(identifier)
						.dataSource(dataSource)
						.onComplete(onComplete)
						.build())
					.forEach(workItems::add);

				workItems.flush();

				/* Wait for the work to finish before returning */

				try
				{
					taskScope.join();
				}
				catch (InterruptedException e)
				{
					this.logger.error("Thread pool was interrupted", e);
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}

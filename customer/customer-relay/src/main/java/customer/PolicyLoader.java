package customer;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.Map;
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
	private static final int POLICIES_PER_QUERY = 100;

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


	public void process(INowRelayRequest request, Consumer<INowRelayResult> onComplete, Stream<String> identifiers)
	{
		//-----------------------------------------------------------------------------
		// var connectionPool = this.connectionPools.get(request.sourceDatabaseName())

		var config = new HikariConfig();
		config.setJdbcUrl("jdbc:as400://10.100.1.250/ITSDBSTAGE");
		config.setDriverClassName("com.ibm.as400.access.AS400JDBCDriver");
		config.setUsername("ITSUSER");
		config.setPassword(System.getenv("RELAY_STAGE")); // <--- *****************!!

		//-----------------------------------------------------------------------------

		try (var dataSource = new HikariDataSource(config))
		{
			/* Assemble a batch of policies to fetch in a single query */
			var workItems = new Batcher<RelayWorkItem>(POLICIES_PER_QUERY, batch ->
			{
				this.logger.info("Connecting to database");
				try (var connection = dataSource.getConnection())
				{
					/* Add a placeholder for each identifier to the SQL query */
					var placeholders = "?,".repeat(batch.size() - 1) + "?";
					var query = String.format(QUERY, placeholders);
					this.logger.debug("Sending `{}`", query);

					try (var statement = connection.prepareStatement(query))
					{
						/* Assign entity identifiers to all the placeholders */
						for (var i = 0; i < batch.size(); ++i)
							statement.setString(i + 1, batch.get(i).identifier());

						ResultSetEx.of(statement.executeQuery())
							.stream()
							.distinct(this::extractCustomerIdentifier)
							.forEach(row -> {
								var identifier = this.extractCustomerIdentifier(row);
								var module = requireNonNull((String)row.get("MODULE"));
								this.logger.info("Loading {} ({})", identifier, module);

								var workItem = ListEx.findFirst(batch, item ->
									identifier.equals(item.identifier()))
									.orElseThrow();

								// HERE - PROCESS THE ROW, GET A RESULT

								var result = workItem.complete();
								onComplete.accept(result);
								this.logger.info("Completed {} ({})", identifier, module);
							});
					}
				}
				catch (SQLException e)
				{
					throw ExceptionEx.toUnchecked(e);
				}
			});

			/* Send through the list of identifiers and do the work */
			identifiers
				.map(identifier -> new RelayWorkItem(request, identifier))
				.forEach(workItems::add);

			workItems.flush();
		}
	}
}

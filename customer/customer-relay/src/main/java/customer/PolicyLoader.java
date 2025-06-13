package customer;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.its.Batcher;
import com.its.ExceptionEx;
import com.its.ResultSetEx;
import com.its.insurancenow.INowEntityLoader;
import com.its.insurancenow.INowRelayRequest;
import com.its.insurancenow.INowRelayResult;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PolicyLoader implements INowEntityLoader
{
	private static final String QUERY =
		"select * from PMSP0200 where concat(TRIM(SYMBOL),POLICY0NUM) in (%s) order by MODULE desc";

	/* Round trips to the database server are SLOW, so lump policies together and
	 * query for their rows in a single request, pulling them apart on this side
	 * where things run much faster  */
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
	private String convertToCustomerIdentifier(Map<String, Object> row)
	{
		var symbol = requireNonNull((String)row.get("SYMBOL")).trim();
		var policy = requireNonNull((String)row.get("POLICY0NUM"));
		return String.join("-", symbol, policy);
	}


	/**
	 * It's easiest for us to look for `CONCAT(TRIM(SYMBOL),POLICY0NUM)`. Adjust any
	 * of the common patterns the customer uses to identify entities to match this
	 * desired format.
	 */
	private String convertToDatabaseIdentifier(String identifier)
	{
		return identifier
			.replace("-", "")
			.replace(" ", "");
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
			var idBatcher = new Batcher<String>(POLICIES_PER_QUERY, batch ->
			{
				this.logger.info("Connecting to database");
				try (var connection = dataSource.getConnection())
				{
					/* Add a placeholder for eac
					h identifier to the SQL query */
					var placeholders = "?,".repeat(batch.size() - 1) + "?";
					var query = String.format(QUERY, placeholders);
					this.logger.debug("Sending `{}`", query);

					try (var statement = connection.prepareStatement(query))
					{
						/* Assign entity identifiers to all the placeholders */
						for (var i = 0; i < batch.size(); ++i)
							statement.setString(i + 1, batch.get(i));

						ResultSetEx.of(statement.executeQuery())
							.stream()
							.forEach(row -> {
								// HERE - PROCESS THE ROW, GET A RESULT

								var identifier = this.convertToCustomerIdentifier(row);
								var module = requireNonNull((String)row.get("MODULE"));
								this.logger.info("Loaded {} ({})", identifier, module);

								var result = INowRelayResult.builder()
									.identifier(identifier)
									.entityType(request.entityType())
									.startedAt(ZonedDateTime.now())
									.finishedAt(ZonedDateTime.now())
									.build();

								onComplete.accept(result);
							});
					}
				}
				catch (SQLException e)
				{
					throw ExceptionEx.toUnchecked(e);
				}
			});

			identifiers
				.map(this::convertToDatabaseIdentifier)
				.forEach(idBatcher::add);

			idBatcher.flush();
		}
	}
}

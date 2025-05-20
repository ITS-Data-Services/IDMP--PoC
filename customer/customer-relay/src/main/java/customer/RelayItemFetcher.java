package customer;

import java.io.OutputStream;
import java.io.PrintWriter;

import javax.inject.Inject;

import com.its.insurance.EntityType;
import com.its.insurancenow.INowRelayItemFetchable;

public class RelayItemFetcher implements INowRelayItemFetchable
{
	/* Equivalent to the v2 Structures.xml, but built programmatically */
	/* private final StructuredDataSqlSourceSchema policySchema */

	@Inject
	public RelayItemFetcher()
	{
		/* Build up a schema something like this? this.policySchema = Element.of("PMSP0200")
		 * .query("select * from PMSP0200 where SYMBOL='{left-of:-:IDENTIFIER:}' and...")
		 * .containing( Element.of("ASBACPP") .query("select * from ASBACPP where ..."),
		 * Element.of("ASBQCPP") .query("select * from ASBQCPP where ...")) */
	}


	/**
	 * Fetch an individual policy, shell, etc. from the customer's source databases.
	 *
	 * @implNote This code must be kept thread-safe.
	 */
	@Override
	public void fetch(EntityType entityType, String identifier, OutputStream outputStream)
	{
		/* For now, just fake it and hardcode a little XML to show it's working */
		try (var writer = new PrintWriter(outputStream))
		{
			writer.format("<%s><IDENTIFIER>%s</IDENTIFIER></%s>", entityType, identifier, entityType);
			writer.println();
		}

		/* But might end up looking something like this... // figure out which source schema to
		 * build // default logic, different clients may do this differently
		 * StructuredDataSqlSourceSchema schema = ... // rather than build anything in-memory we'll
		 * just stream the assembled // XML document right to the output stream, SAX style var
		 * documentBuilder = new StreamingXmlBuilder(outputStream) // Single-use-case replacement
		 * for multipurpose v2 DDR // Would inject an instance, could also have a
		 * TableDataDatabaseReader(), etc. var reader = new StructuredDataDatabaseReader() // Runs
		 * the queries and calls SAX-style methods on the data document builder, // which just
		 * writes directly to the provided output stream reader.fetch(schema, identifier,
		 * documentBuilder) */
	}
}

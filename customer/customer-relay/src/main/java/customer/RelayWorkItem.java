package customer;

import java.time.ZonedDateTime;

import com.its.StringEx;
import com.its.insurancenow.INowRelayRequest;
import com.its.insurancenow.INowRelayResult;

/**
 * Roll up context about an item being relayed.
 */
public class RelayWorkItem
{
	private final INowRelayRequest request;
	private final String identifier;
	private final ZonedDateTime startedAt;


	public RelayWorkItem(INowRelayRequest request, String identifier)
	{
		this.request = request;
		this.identifier = normalizeIdentifier(identifier);
		this.startedAt = ZonedDateTime.now();
	}


	/**
	 * Customers and analysts don't always give us identifiers in the format
	 * we'd like. Normalize these cases into our preferred format.
	 */
	public static String normalizeIdentifier(String identifier)
	{
		var ret = identifier.replace(" ", "");

		if (!ret.contains("-"))
		{
			var symbolLength = ret.startsWith("HOM") ? 3 : 2;
			ret = StringEx.insertAt(ret, symbolLength, "-");
		}

		return ret;
	}


	public INowRelayResult complete()
	{
		return INowRelayResult.builder()
			.identifier(this.identifier)
			.entityType(this.request.entityType())
			.startedAt(this.startedAt)
			.finishedAt(ZonedDateTime.now())
			.build();
	}


	public String identifier()
	{
		return this.identifier;
	}
}

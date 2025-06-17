package customer;

import static java.util.Objects.requireNonNull;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

import com.its.StringEx;
import com.its.insurancenow.INowRelayRequest;
import com.its.insurancenow.INowRelayResult;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Roll up context about an item being relayed.
 */
public class RelayWorkItem
{
	public static class Builder
	{
		private @Nullable HikariDataSource dataSource;
		private @Nullable INowRelayRequest request;
		private @Nullable String identifier;
		private @Nullable Consumer<INowRelayResult> onComplete;


		public RelayWorkItem build()
		{
			return new RelayWorkItem(this);
		}


		public Builder dataSource(HikariDataSource dataSource)
		{
			this.dataSource = dataSource;
			return this;
		}


		public Builder identifier(String identifier)
		{
			this.identifier = identifier;
			return this;
		}


		public Builder onComplete(Consumer<INowRelayResult> onComplete)
		{
			this.onComplete = onComplete;
			return this;
		}


		public Builder request(INowRelayRequest request)
		{
			this.request = request;
			return this;
		}
	}

	private final HikariDataSource dataSource;
	private final INowRelayRequest request;
	private final String identifier;
	private final ZonedDateTime startedAt;
	private final Consumer<INowRelayResult> onComplete;


	private RelayWorkItem(Builder builder)
	{
		this.dataSource = requireNonNull(builder.dataSource);
		this.request = requireNonNull(builder.request);
		this.identifier = normalizeIdentifier(requireNonNull(builder.identifier));
		this.onComplete = requireNonNull(builder.onComplete);

		this.startedAt = ZonedDateTime.now();
	}


	public static Builder builder()
	{
		return new Builder();
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


	public void complete()
	{
		this.onComplete.accept(INowRelayResult.builder()
			.identifier(this.identifier)
			.entityType(this.request.entityType())
			.startedAt(this.startedAt)
			.finishedAt(ZonedDateTime.now())
			.build());
	}


	public HikariDataSource dataSource()
	{
		return this.dataSource;
	}


	public String identifier()
	{
		return this.identifier;
	}
}

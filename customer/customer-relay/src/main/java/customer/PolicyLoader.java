package customer;

import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.its.insurancenow.INowEntityLoader;
import com.its.insurancenow.INowRelayRequest;
import com.its.insurancenow.INowRelayResult;

public class PolicyLoader implements INowEntityLoader
{
	private final Logger logger;


	@Inject
	public PolicyLoader(Logger logger)
	{
		this.logger = logger;
	}


	public void process(INowRelayRequest request, Consumer<INowRelayResult> onComplete, Stream<String> identifiers)
	{
		/* For now, just fail everything that gets sent to us */

		identifiers.forEach(identifier ->
		{
			this.logger.warn("Unable to relay {}; not yet implemented", identifier);

			var result = INowRelayResult.builder()
				.identifier(identifier)
				.entityType(request.entityType())
				.startedAt(ZonedDateTime.now())
				.finishedAt(ZonedDateTime.now())
				.failure(new UnsupportedOperationException("Feature is not yet implemented."))
				.build();

			onComplete.accept(result);
		});
	}
}

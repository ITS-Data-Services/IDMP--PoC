package customer;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.its.insurancenow.INowEntityLoader;
import com.its.insurancenow.INowRelayRequest;

public class PolicyLoader implements INowEntityLoader
{
	private final Logger logger;


	@Inject
	public PolicyLoader(Logger logger)
	{
		this.logger = logger;
	}


	public void process(INowRelayRequest request, Stream<String> identifiers)
	{
		this.logger.info("Processing a bunch of identifiers for job {}", request.jobId());
		identifiers.forEach(this.logger::info);
	}
}

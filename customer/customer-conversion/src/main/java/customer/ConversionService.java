package customer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.its.idmp.Host;
import com.its.insurancenow.INowConversionService;

@Singleton
public class ConversionService extends INowConversionService
{
	@Inject
	public ConversionService(Host host, Logger logger)
	{
		super(host, logger);
	}
}

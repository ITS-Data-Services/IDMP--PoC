package customer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.its.idmp.Host;
import com.its.insurancenow.INowLoaderService;

@Singleton
public class LoaderService extends INowLoaderService
{
	@Inject
	public LoaderService(Host host, Logger logger)
	{
		super(host, logger);
	}
}

package customer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.its.idmp.Host;
import com.its.insurancenow.INowReportingService;

@Singleton
public class ReportingService extends INowReportingService
{
	@Inject
	public ReportingService(Host host, Logger logger)
	{
		super(host, logger);
	}
}

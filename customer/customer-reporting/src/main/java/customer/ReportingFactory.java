package customer;

import javax.inject.Singleton;

import com.its.idmp.ServiceFactory;
import com.its.insurancenow.INowReportingModule;

import dagger.Component;

/**
 * Dagger factory definition for the client reporting service.
 */
@Singleton
@Component(modules = {CustomerModule.class, INowReportingModule.class})
public interface ReportingFactory extends ServiceFactory
{
	@Override
	ReportingService createService();
}

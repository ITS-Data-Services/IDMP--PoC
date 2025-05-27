package customer;

import javax.inject.Singleton;

import com.its.idmp.ServiceFactory;
import com.its.insurancenow.INowLoaderModule;

import dagger.Component;

/**
 * Dagger factory definition for the client loader service.
 */
@Singleton
@Component(modules = {CustomerModule.class, INowLoaderModule.class})
public interface LoaderFactory extends ServiceFactory
{
	@Override
	LoaderService createService();
}

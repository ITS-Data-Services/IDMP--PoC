package customer;

import javax.inject.Singleton;

import com.its.idmp.ServiceFactory;
import com.its.insurancenow.INowRegistrarModule;
import com.its.insurancenow.INowRegistrarService;

import dagger.Component;

/**
 * Dagger factory definition for the client registrar service.
 */
@Singleton
@Component(modules = {CustomerModule.class, INowRegistrarModule.class})
public interface RegistrarFactory extends ServiceFactory
{
	@Override
	INowRegistrarService createService();
}

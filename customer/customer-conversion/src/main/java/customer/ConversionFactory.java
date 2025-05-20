package customer;

import javax.inject.Singleton;

import com.its.idmp.ServiceFactory;
import com.its.insurancenow.INowConversionModule;

import dagger.Component;

/**
 * Dagger factory definition for the client conversion service.
 */
@Singleton
@Component(modules = {CustomerModule.class, INowConversionModule.class})
public interface ConversionFactory extends ServiceFactory
{
	@Override
	ConversionService createService();
}

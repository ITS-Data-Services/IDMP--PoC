package customer;

import javax.inject.Singleton;

import com.its.idmp.ServiceFactory;
import com.its.insurancenow.INowRelayService;

import dagger.Component;

/**
 * Dagger factory definition for the client relay service.
 */
@Singleton
@Component(modules = {RelayModule.class})
public interface RelayFactory extends ServiceFactory
{
	@Override
	INowRelayService createService();
}

package customer;

import com.its.insurancenow.INowSettings;

import dagger.Binds;
import dagger.Module;

/**
 * Provide dependency injection bindings for client specific classes.
 */
@Module
public interface CustomerModule
{
	@Binds
	INowSettings bindINowSettings(CustomerSettings settings);
}

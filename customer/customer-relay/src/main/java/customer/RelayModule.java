package customer;

import com.its.insurance.EntityType;
import com.its.insurancenow.INowEntityLoader;
import com.its.insurancenow.INowRelayModule;

import dagger.Binds;
import dagger.MapKey;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module(includes = {CustomerModule.class, INowRelayModule.class})
public interface RelayModule
{
	@MapKey
	@interface EntityTypeKey
	{
		EntityType value();
	}

	@Binds
	@IntoMap
	@EntityTypeKey(EntityType.POLICY)
	INowEntityLoader bindPolicyLoader(PolicyLoader policyLoader);
}

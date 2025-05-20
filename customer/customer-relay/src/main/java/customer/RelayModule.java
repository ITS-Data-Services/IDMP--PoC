package customer;

import com.its.insurancenow.INowRelayItemFetchable;
import com.its.insurancenow.INowRelayModule;

import dagger.Binds;
import dagger.Module;

@Module(includes = {CustomerModule.class, INowRelayModule.class})
public interface RelayModule
{
	@Binds
	INowRelayItemFetchable bindRelayItemFetchable(RelayItemFetcher fetcher);
}

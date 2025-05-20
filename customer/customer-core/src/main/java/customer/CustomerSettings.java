package customer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.its.insurancenow.INowSettings;

@Singleton
public class CustomerSettings extends INowSettings
{
	@Inject
	public CustomerSettings()
	{
		/* DI placeholder */
	}


	@Override
	public String customer()
	{
		return "Jess";
	}
}

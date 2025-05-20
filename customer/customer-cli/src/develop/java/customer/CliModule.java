package customer;

import com.its.insurancenow.INowCliModule;

import dagger.Module;

@Module(includes = {CustomerModule.class, INowCliModule.class})
public interface CliModule
{
	/* Placeholder for future expansion */
}

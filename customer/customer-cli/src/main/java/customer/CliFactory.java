package customer;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Dependency injection component for the client's CLI.
 */
@Singleton
@Component(modules = {CliModule.class})
public interface CliFactory
{
	Cli cli();
}

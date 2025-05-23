package customer;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.its.idmp.CliCommand;
import com.its.idmp.Idmp;
import com.its.idmp.IdmpCli;

@Singleton
public class Cli extends IdmpCli
{
	@Inject
	public Cli(Set<CliCommand> commands)
	{
		super(commands);
	}


	public static void main(String[] args)
	{
		Idmp.configure();
		DaggerCliFactory.create()
			.cli()
			.launch(args);
	}
}

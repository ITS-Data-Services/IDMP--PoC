package customer;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.its.idmp.CliCommand;
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
		var cli = DaggerCliFactory.create().cli();
		cli.launch(args);
	}
}

package Server;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Client.ResourceClient;
public class ServerArgumentParser {
	private CommandLine commandLine;
	private String[] args;
	private ServerCommands commands;
	public ServerArgumentParser(String[] argument)
	{
		commands = new ServerCommands();
		args = argument;
	}
	
	void parseInput() throws NumberFormatException{
	      
	      
	      Options svOptions = new Options();
		    
	      Option advertisedhostname = new Option("advertisedhostname", true,"advertisedhostname");
	      advertisedhostname.setRequired(false);
	      svOptions.addOption(advertisedhostname);

	      Option connectionintervallimit = new Option("connectionintervallimit", true,"connectionintervallimit");
	      connectionintervallimit.setRequired(false);
	      svOptions.addOption(connectionintervallimit);

	      Option svPort = new Option("port", true, "server port, an integer");
	      svPort.setRequired(true);
	      svOptions.addOption(svPort);

	      Option svSecret = new Option("secret", true , "secret");
	      svSecret.setRequired(false);
	      svOptions.addOption(svSecret);

	      Option svDebug = new Option("debug", "print debug information");
	      svDebug.setRequired(false);
	      svOptions.addOption(svDebug);
		    
		  Option svExchangeInterval = new Option("exchangeinterval", true, "exchange interval in seconds");
		  svExchangeInterval.setRequired(false);
		  svOptions.addOption(svExchangeInterval);
		  
	      
	      CommandLineParser commandLineParser = new DefaultParser();
	      HelpFormatter helpFormatter = new HelpFormatter();

	      try {
		    commandLine = commandLineParser.parse(svOptions, args);
		    updateCommands();
	      } catch (ParseException e) {
		    System.out.println(e.getMessage());
		    helpFormatter.printHelp("Server-start manual,", svOptions);

		    System.exit(1);
	      }
	      
	}
	private void updateCommands() throws NumberFormatException
	{
		String advertisedHostName = commandLine.getOptionValue("advertisedhostname");
		int connectioninterval;
		if(commandLine.hasOption("connectionintervallimit") )
		connectioninterval = Integer.parseInt(commandLine.getOptionValue("connectionintervallimit"));
		else 
			connectioninterval = Parameters.CONNECTION_INTERVAL;
		int exchangeinterval;
		if(commandLine.hasOption("exchangeinterval"))
			exchangeinterval = Integer.parseInt(commandLine.getOptionValue("exchangeinterval"));
		else 
			exchangeinterval = Parameters.EXCHANGE_INTERVAL;
		int port = Integer.parseInt(commandLine.getOptionValue("port"));
		String secret;
		if(commandLine.hasOption("secret"))
			secret = commandLine.getOptionValue("secret");
		else {
			String uuid = UUID.randomUUID().toString();
			secret = uuid.split("-")[0];
		}
		commands.setSecret(secret);
		
		boolean debug;
		debug = commandLine.hasOption("debug");
		
		commands = new ServerCommands(advertisedHostName, connectioninterval, exchangeinterval,
				port, secret, debug);
		
	}
	public ServerCommands getCommands()
	{
		return commands;
	}
	
	
}

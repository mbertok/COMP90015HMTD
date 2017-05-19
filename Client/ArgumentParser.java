package Client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Server.ObjectServer;
import Server.ServerTCP;

public class ArgumentParser {
	private CommandLine commandLine;
	private String[] args;
	private String command;
	ResourceClient tempResource;
	ArrayList<ObjectServer> ServerList;
	public ArgumentParser(String[] argument)
	{
		command = " ";
		args = argument;
		ServerList = new ArrayList<ObjectServer>();
		tempResource = new ResourceClient(null,null,"test");
	}
	void parseInput() throws NumberFormatException{
		Options options = new Options();

	    Option channel = new Option("channel", true, "channel");
	    channel.setRequired(false);
	    channel.setArgName("Channel name as String");
	    options.addOption(channel);

	    Option debug = new Option("debug", "print debug information");
	    debug.setRequired(false);
	    options.addOption(debug);

	    Option description = new Option("description", true,"resource description");
	    description.setRequired(false);
	    description.setArgName("DESCRIPTION AS STRING");
	    options.addOption(description);
	    
	    Option exchange = new Option("exchange",false,"exchange server list with server");
	    exchange.setRequired(false);
	    options.addOption(exchange);
	    
	    Option fetch = new Option("fetch", false,"fetch resources from server");
	    fetch.setRequired(false);
	    fetch.setArgName("URI AS STRING");
	    options.addOption(fetch);
	    
	    Option host = new Option("host", true,"server host, a domain name or IP address");
	    host.setRequired(true);
	    host.setArgName("SERVER NAME AS STRING");
	    options.addOption(host);
	    
	    Option name = new Option("name",true,"resource name");
	    name.setRequired(false);
	    name.setArgName("RESOURCE NAME AS STRING");
	    options.addOption(name);
	    
	    Option owner = new Option("owner", true,"owner");
	    owner.setRequired(false);
	    owner.setArgName("OWNER AS STRING");
	    options.addOption(owner);
	    
	    Option port = new Option("port", true ,"server port, an integer");
	    port.setRequired(true);
	    port.setArgName("INT");
	    options.addOption(port);
	    
	    Option publish = new Option("publish",false,"publish resource on server");
	    publish.setRequired(false);
	    options.addOption(publish);
	    
	    Option query = new Option("query", false,"query for resources on server");
	    query.setRequired(false);
	    options.addOption(query);
	    
	    Option remove = new Option("remove", false,"remove resource from server");
	    remove.setRequired(false);
	    options.addOption(remove);
	    
	    Option secret = new Option("secret", true, "secret");
	    secret.setRequired(false);
	    secret.setArgName("SECRET AS LONG STRING");
	    options.addOption(secret);
	    
	    Option servers = Option.builder("servers")
	    		.hasArgs()
	    		.valueSeparator(',')
	    		.build();
	    options.addOption(servers);
	    
	    Option share = new Option("share",false,"share resource on server");
	    share.setRequired(false);
	    options.addOption(share);
	    
	    Option tags = Option.builder("tags")
	    		.desc("resource tags, tag1, tag2, tag3,...")
			.hasArgs()
			.valueSeparator(',')
			.build();
	    options.addOption(tags);
	    
	    Option uri = new Option("uri", true,"resource URI");
	    uri.setDescription("resource URI");
	    uri.setRequired(false);
	    options.addOption(uri);
	    
		Option unsubscribe = new Option("unsubscribe", false, "query for resources on server");
		unsubscribe.setRequired(false);
		options.addOption(unsubscribe);

		Option subscribe = new Option("subscribe", false, "query for resources on server");
		subscribe.setRequired(false);
		options.addOption(subscribe);

        Option id = new Option("id", true,"subscription id");
        owner.setRequired(true);
        options.addOption(id);
	    
	    CommandLineParser commandLineParser = new DefaultParser();
	    HelpFormatter helpFormatter = new HelpFormatter();

	    try {
	     commandLine = commandLineParser.parse(options, args);
	    } catch (ParseException e) {
	     System.out.println(e.getMessage());
	     helpFormatter.printHelp("utility-name", options);

	     System.exit(1);
	    }

	    String[] allCMD = {"publish","share","remove","query","fetch",
	    		"exchange","subscribe","unsubscribe"};
	    
	    int count = 0;
	    for (int i = 0; i < allCMD.length; ++i){
		  if (commandLine.hasOption(allCMD[i])) {
			command = allCMD[i].toUpperCase();
			count++;
		  }
	    }
	    if (count != 1) {
	    command = " ";
	    }
	    updateLocalObjects();

	}
	private void updateLocalObjects() throws NumberFormatException
	{
		switch(command)
		{
		case "PUBLISH":
		case "SHARE":
		case "REMOVE":
		case "QUERY":
		case "FETCH":
		case "SUBSCRIBE":
			updateTempResource();
			break;
		case "EXCHANGE":
			updateServerList();
			break;
		default:
			
			break;
		}
	}
	public JSONObject toJSON()
	{
		JSONObject tempJSONObject = new JSONObject();
		JSONArray tempJSONArray;
		tempJSONObject.put("command", command);
		switch(command)
		{
		case "PUBLISH":
		case "SHARE":
		case "REMOVE":
			JSONObject psr = tempResource.toJSON(); 
			tempJSONObject.put("resource", psr);
			break;
		case "QUERY":
		case "FETCH":
			JSONObject qf = tempResource.toJSON(); 
			tempJSONObject.put("resourceTemplate", qf);
			break;
		case "EXCHANGE":
			JSONArray ex = serverListToJSON();
			tempJSONObject.put("serverList", ex);
			break;
		case  "SUBSCRIBE":
			JSONObject sf = tempResource.toJSON();
			String id=checkId();
            tempJSONObject.put("id",id);
			tempJSONObject.put("resourceTemplate", sf);
			break;
		default:
			
			break;
		}
		if(command.equals("SHARE"))
		{
			String secret = commandLine.getOptionValue("secret");
			tempJSONObject.put("secret", secret);		
		}
		
		return tempJSONObject;
		
	}
	
	private String checkId()
	{
		String id=null;
		if(commandLine.hasOption("id"))
        {
		  id=commandLine.getOptionValue("id");
        }
		else 
		{
			System.out.println("Subscription command needs id");
			System.exit(1);
		}
		return id;
	}
	private void updateTempResource()
	{
		String name = getValue("name");
		String[] tags = commandLine.getOptionValues("tags");
		String description = getValue("description");
		String uri = getValue("uri");
		String channel = getValue("channel");
		String owner = getValue("owner");
		String host = getValue("host");
		int port = Integer.parseInt(commandLine.getOptionValue("port"));
		System.out.println("host:"+host+"port"+port);
		ObjectServer ezserver = new ObjectServer(host,port);
		tempResource = new ResourceClient(name,tags,description,uri,
				channel, owner,ezserver);	
	}
	private void updateServerList() throws NumberFormatException
	{
		String[] servers = commandLine.getOptionValues("servers");
		for(String s:servers)
		{
			if(s.contains(":"))
			{
			ObjectServer temp = new ObjectServer(s.split(":")[0] , Integer.parseInt(s.split(":")[1]));
			ServerList.add(temp);
			}
		}
		
	}
	private JSONArray serverListToJSON()
	{
		JSONArray tempArray = new JSONArray();
		for(ObjectServer s:ServerList)
		{
			JSONObject tempObject = null;
			if( s!= null )
			{
				tempObject = new JSONObject();
				tempObject.put("hostname", s.getHostname());
				tempObject.put("port", s.getPortString());
				tempArray.add(tempObject);
//				System.out.print(tempObject.toString());
			}
		}
		return tempArray;	
	}
	int getPort(){
		int temp = Integer.parseInt(commandLine.getOptionValue("port"));
		return temp;
	}
	InetAddress getHostName() throws UnknownHostException
	{
		String temp = commandLine.getOptionValue("host");
		InetAddress test = InetAddress.getByName(temp);
		return test;
		
	}
	boolean getDebug()
	{
		boolean temp = commandLine.hasOption("debug");
		return temp;
	}
	String getValue(String label)
	{
		return (commandLine.getOptionValue(label)==null?"":commandLine.getOptionValue(label));
	}
}

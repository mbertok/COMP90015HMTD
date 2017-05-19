package Server;
import java.net.*;
import java.util.ArrayList;
import java.net.URISyntaxException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
public class Connection extends Thread {
	  DataInputStream in;
	  DataOutputStream out;
	  Socket clientSocket;
	  Services availableServices;
	  JSONParser parser;
	  VerifyRequestObject verify;
	  Response reply;
	  boolean debug;
	  String cmdText;
	  int hits;
	  /**
	 * @return the hits
	 */
	public int getHits() {
		return hits;
	}
	/**
	 * @param hits the hits to set
	 */
	public void setHits(int hits) {
		this.hits = hits;
	}
	public Connection (Socket aClientSocket, Services aS,boolean debug) {
	    try {
	    	  this.debug = debug;
	    	  availableServices = aS;
	      clientSocket = aClientSocket;
	      in = new DataInputStream( clientSocket.getInputStream());
	      out = new DataOutputStream( clientSocket.getOutputStream());
	      parser = new JSONParser();
	      verify = new VerifyRequestObject();
	      reply = null;
	      cmdText = " ";
	      hits = 0;
	    } catch(IOException e) {
	       System.out.println("Connection:"+e.getMessage());
	} 
	    }
	public void run(){
		System.out.println("Client Connected!"); 
			boolean waitForMessage = false;     
		     try {
		     do
			{	    	
					waitForMessage = serveClient();
					if(!cmdText.equals("UNSUBSCRIBE") && waitForMessage == false)
				     	{
						send(reply);
						if(cmdText.equals("SUBSCRIBE"))
							waitForMessage = true;
//							System.out.println("Waiting for unsubscribe");
				     	}
			}while(waitForMessage == true);
		     } catch (ParseException e){
				reply = new Response(false,"Invalid Resource Template");
			}catch (IOException e) {
				e.printStackTrace();
			}
	}
	boolean serveClient() throws IOException, ParseException
	{
		boolean result = true;
		 if(in.available() > 0){
	    		// Attempt to convert read data to JSON
	    	 	JSONObject command = (JSONObject) parser.parse(in.readUTF());
	    	 	if(debug)
	    		System.out.println("Received: "+command.toJSONString());		
	    		if(verify.existsCommand(command))
	  		{
	    			cmdText = command.get("command").toString();
	    			synchronized(verify){
	    			if(verify.checkResource(command, cmdText))
	    			{
	    				performOperation(command);
	    			}
	    			else
	    			{
	    				reply = verify.getMissingResponse(cmdText);
	    			}
	    			}
	  		}
	  		else
	  		{
	  			reply = new Response(false,"Command missing");
	  		}
	    			result = false;
	    		
	  	}
		return result;
	}
	
	void performOperation(JSONObject command)
	{
		try {
		String commandText = command.get("command").toString();
		commandText = commandText.toUpperCase();
		synchronized(availableServices)
		{
		switch(commandText)
		{
		case "FETCH": 
//			System.out.println("FETCH COMMAND RECEIVED");//debug
			reply = availableServices.fetch(new ResourceServer(command,commandText), out);
			break;
		case "QUERY":
//			System.out.println("QUERY COMMAND RECEIVED");//debug
			boolean relay;
			relay = getRelay(command);
			ResourceServer temp = new ResourceServer(command,commandText);
			reply = availableServices.
					query(relay, temp);
			break;
		case "PUBLISH":
//			System.out.println("PUBLISH COMMAND RECEIVED");//debug
			reply = availableServices.publish(new ResourceServer(command,commandText));
			if(reply.getResponse().equals("success"))
			{	
				new SendSubscribe(availableServices,new ResourceServer(command, commandText))
				.start();
			}
			break;	
		case "SHARE":
//			System.out.println("SHARE COMMAND RECEIVED");//debug
			reply = availableServices.share(command.get("secret").toString(),
					new ResourceServer(command,commandText));
			break;	
		case "REMOVE":
//			System.out.println("REMOVE COMMAND RECEIVED");//debug
			reply = availableServices.remove(new ResourceServer(command,commandText));
			break;	
		case "EXCHANGE":
//			System.out.println("EXCHANGE COMMAND RECEIVED");//debug
			reply = initiateExchange(command);
			break;
		case "SUBSCRIBE":
			System.out.println("SUBSCRIBE COMMAND RECEIVED");
			String Id = getId(command);
			reply = availableServices.Subscribe(this, new ResourceServer(command, commandText), Id);
			hits += reply.getResourceList().size();
			break;
		case "UNSUBSCRIBE":
			System.out.println("UNSUBSCRIBE COMMAND RECEIVED");
			String ID = getId(command);
			availableServices.UnSubscribe(this, ID);
			break;
		default:
			reply =  new Response(false,"invalid command");
			break;
		}
		}
		  }catch (ParseException e){
			  reply = new Response(false, "invalid resourceTemplate");
		  }
		catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid Input");
			}
		catch (IOException e)
		{
			System.out.println("Client Disconnected!");
		}
	}
	Response initiateExchange(JSONObject command) throws ParseException, IOException
	{
		Response reply = null;
		JSONArray list = getList(command);
		if(list != null)
		{
		reply = availableServices.exchange(list);
		}
		else
		{
			reply = new Response(false, "missing or invalid server list");
		}
		return reply;
	}
	void sendJSON(JSONObject toSend) throws IOException
	{
		out.writeUTF(toSend.toJSONString());
		if(debug)
			{
			System.out.println(toSend.toJSONString());
			}
		out.flush();
	}
	void sendResource(ResourceServer toSend) throws IOException
	{
		JSONObject temp = toSend.toJSON();
		out.writeUTF(temp.toJSONString());
		hits++;
		if(debug)
			{
			System.out.println(temp.toJSONString());
			}
		out.flush();
	}
	void send(Response toSend) throws IOException
	{
		
		if(toSend != null)
		{
		JSONObject reply = toSend.toJSON();
		if(toSend.getId()!=null)
		{
			reply.put("id", toSend.getId());
		}
		out.writeUTF(reply.toJSONString());
		if(debug)
		System.out.println("Sent:"+reply.toString());
		
		if(toSend.responseListIsEmpty() == false)
		{
			out.writeUTF(reply.toJSONString());
			JSONObject temp = null;
			int iterator = 0;
			ArrayList<JSONObject> replyList = toSend.getResponseListToJSON();
			for(JSONObject j:replyList)
			{
				iterator++;
				out.writeUTF(j.toJSONString());
				if(debug)
					System.out.println(j.toString());
			}
			if(cmdText.equals("SUBSCRIBE")==false)
			{
				temp = new JSONObject();
				temp.put("resultSize", iterator);
				out.writeUTF(temp.toJSONString());
				if(debug)
				{
					System.out.println("Sent:"+temp.toString());
				}
				out.flush();
			}
		}
		else {
			out.writeUTF(reply.toJSONString());
			if(debug)
				{
				reply.toJSONString();
				}
			out.flush();
		}
		}
		}
		
	JSONArray getList(JSONObject command) throws IOException{
		JSONArray list = (JSONArray) command.get("serverList");
		return list;
	}
	boolean getRelay(JSONObject command){
		String relayText;
		boolean relay = Parameters.RELAY;
		if(command.containsKey("relay"))
		{
		relayText = command.get("relay").toString();
		if(relayText.equals("true"))
		{
			relay = true;
		}
		else{
			relay = false;
		}
		}
		return relay;
	}
	
	String getId(JSONObject command){
		String id = null;
		if(command.containsKey("id")){
			if(command.get("id")!=null)
			{
			id = command.get("id").toString();
			}
		}
		return id;
	}
	/*
	 * To close the input and output data streams of the connection
	 */
	void close() throws IOException
	{
		in.close();
		out.close();
	}
		}



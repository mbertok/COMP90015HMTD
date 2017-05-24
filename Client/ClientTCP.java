package Client;
import java.net.*;
import java.util.Arrays;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.*;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Server.Response;
public class ClientTCP extends Thread {
	int serverPort;
	InetAddress serverAddress;
	Socket insecureClientSocket;
	SSLSocket secureClientSocket;
	String clientArgument[];
	JSONParser parser;
	DataInputStream in;
	DataOutputStream out;
	JSONObject newCommand;
	GetInput userInput;
	boolean debug;
	boolean isSecure;
	String subscribeId;
	ClientTCP(JSONObject cmd, int port, InetAddress server, boolean debug, boolean secure)
	{
		try{
		this.isSecure = secure;
		this.debug = debug;
		this.serverPort = port;
		this.serverAddress = server;
		this.newCommand = cmd;
		this.parser = new JSONParser();
		
		if(isSecure)
		{
			System.setProperty("javax.net.ssl.trustStore", "clientKeystore/clientTruststore.jks");  
			System.setProperty("javax.net.ssl.trustStorePassword","comp90015");   
			System.setProperty("javax.net.ssl.keyStore","clientKeystore/clientKeystore.jks");
			System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
//			System.setProperty("javax.net.debug","all");
		    SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			secureClientSocket = (SSLSocket) sslsocketfactory.createSocket(serverAddress, serverPort);
			this.in = new DataInputStream( secureClientSocket.getInputStream());
		    this.out =new DataOutputStream( secureClientSocket.getOutputStream());
		}
		else
		{
			insecureClientSocket = new Socket(server, serverPort);
			this.in = new DataInputStream( insecureClientSocket.getInputStream());
		    this.out =new DataOutputStream( insecureClientSocket.getOutputStream());
		}
	    
	    this.subscribeId = null;
	    if(newCommand.containsKey("id"))
	    {
	    	this.subscribeId = newCommand.get("id").toString();
	    }
	    this.userInput = new GetInput(this.out,subscribeId);
		}catch(IOException e){}
	}
	public void run()
	{
	  try{
		  
	    JSONObject response;
		boolean wait = false;
		String commandText = Send();
		response = getResponse();
		String replyResponse = (String) response.get("response");
		wait = (checkCommand(commandText, replyResponse));
    		waitForNextMessage(wait, commandText);
    	
    		
	  }catch (ParseException e)
	     {
		  System.out.println("Parse:"+e.getMessage());
	     }
	  catch (UnknownHostException e) {
	     System.out.println("Socket:"+e.getMessage());
	  }catch (EOFException e){
	     System.out.println("EOF:"+e.getMessage());
	  }catch (IOException e){
	     System.out.println("readline:"+e.getMessage());
	  }catch (NullPointerException e)
	  {
		  System.out.println("");// To debug
	  }
	  finally{
		  try {
			if(isSecure && secureClientSocket != null)
			{
				secureClientSocket.close();
			}
		else if(insecureClientSocket !=null)
			{
			insecureClientSocket.close();
			}
		  } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  }
	  
}
	boolean checkCommand(String command, String response)
	  {
		return (response.equals("success") && (command.equals("QUERY")||command.equals("FETCH")
				||command.equals("SUBSCRIBE")));
	  }
	JSONObject getResponse() throws NullPointerException, IOException, ParseException
	{
		
		String data = in.readUTF();
		// Attempt to convert read data to JSON
		JSONObject response = (JSONObject) parser.parse(data);
		System.out.println("RECEIVED:" + response.toString());
		return response;
		
	}
	String Send() throws IOException
	{
		
	    String commandText = newCommand.get("command").toString();
	    String data;
	    
	    if(commandText.equals("QUERY")||commandText.equals("SUBSCRIBE"))
	    {
	    	newCommand.put("relay", "true");
	    }
		out.writeUTF(newCommand.toJSONString( ));
		out.flush();
		if(debug)
		{System.out.println("Sent:" + newCommand.toJSONString());}
		return commandText;
		
	}
	void waitForNextMessage(boolean wait,String cmd) throws ParseException, IOException{
		if(wait)
		{
			switch(cmd)
			{
			case "QUERY":
				waitForJSONObjects(wait);
				break;
			case "SUBSCRIBE":
				userInput.start();
				waitForJSONObjects(wait);
				break;
			case "FETCH":
				try {
					waitForFetch();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					System.out.println("Cannot Save! Response uri not valid.");
				}
				break;
			default:			
				}
			}
		}
	void waitForJSONObjects(boolean wait) throws IOException, ParseException
	{
		JSONObject response = new JSONObject();
		String data;
		if(wait)
		{
			do
		{
			if(in.available()>0)
			{
				data = in.readUTF();
				// Attempt to convert read data to JSON
				response = (JSONObject) parser.parse(data);
				System.out.println("RECEIVED:"+response.toString());
    			}
		}while(response.containsKey("resultSize") == false);
		}

	}

	void waitForFetch() throws IOException, ParseException, URISyntaxException
	{
			JSONObject response = getResponse();
			if(debug)
			{System.out.println("RECEIVED:"+response.toString());}
			String tempFileName = response.get("uri").toString();
			URI uri = new URI(tempFileName);
			String tempPath = uri.getPath();
			String[] file = tempPath.split("/");
			String fileNameToGet = file[file.length-1];
		// The file location
	String fileName = "client_files/"+fileNameToGet;
	
	// Create a RandomAccessFile to read and write the output file.
	RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");
	
	// Find out how much size is remaining to get from the server.
	long fileSizeRemaining = (Long) response.get("file_size");
	
	int chunkSize = setChunkSize(fileSizeRemaining);
	
	// Represents the receiving buffer
	byte[] receiveBuffer = new byte[chunkSize];
	
	// Variable used to read if there are remaining size left to read.
	int num;
	
//	System.out.println("Downloading "+fileName+" of size "+fileSizeRemaining);
	while((num=in.read(receiveBuffer))>0){
		// Write the received bytes into the RandomAccessFile
		downloadingFile.write(Arrays.copyOf(receiveBuffer, num));
		
		// Reduce the file size left to read..
		fileSizeRemaining-=num;
		
		// Set the chunkSize again
		chunkSize = setChunkSize(fileSizeRemaining);
		receiveBuffer = new byte[chunkSize];
		
		// If you're done then break
		if(fileSizeRemaining==0){
			break;
		}
	}
	System.out.println("File received!");
	downloadingFile.close();
	response = getResponse();
	}
	public static int setChunkSize(long fileSizeRemaining){
		// Determine the chunkSize
		int chunkSize=1024*1024;
		
		// If the file size remaining is less than the chunk size
		// then set the chunk size to be equal to the file size.
		if(fileSizeRemaining<chunkSize){
			chunkSize=(int) fileSizeRemaining;
		}
		
		return chunkSize;
	}
}

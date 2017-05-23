package Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.*;
import org.apache.commons.validator.routines.InetAddressValidator;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class Services {
	private ArrayList<Subscription> SubscriptionList;
	private ArrayList<ObjectServer> ServerList;
	private HashMap<String, ResourceServer> ResourceList;
	private String secretServer;
	private boolean debug;
	/**
	 * @return the serverList
	 */
	public ArrayList<ObjectServer> getServerList() {
		return ServerList;
	}
	/**
	 * @param serverList the serverList to set
	 */
	public void setServerList(ArrayList<ObjectServer> serverList) {
		ServerList = serverList;
	}
	/**
	 * @return the resourceList
	 */
	public HashMap<String, ResourceServer> getResourceList() {
		return ResourceList;
	}
	/**
	 * @param resourceList the resourceList to set
	 */
	public void setResourceList(HashMap<String, ResourceServer> resourceList) {
		ResourceList = resourceList;
	}
	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secretServer;
	}
	/**
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		this.secretServer = secret;
	}
	public Services(String secret)
	{
		if(secret != null)
		this.secretServer =secret;
		debug = true;
		ServerList = new ArrayList<ObjectServer>();
		ResourceList = new HashMap<String, ResourceServer>();
		this.SubscriptionList = new ArrayList<Subscription>();
		
	}
	/* Adds a new item in the SubscriptionList 
	 * @param c - the connection object for the client
	 * @param r - the resource template
	 * @param id - the id of the subscription by the client
	 * @return - the response on successful addition. 
	 */
	public Response Subscribe(Connection c, ResourceServer r, String id, boolean relay)
			throws UnknownHostException, IOException, ParseException
	{
//		System.out.println("In subscribe method");
		Response response; 
		synchronized(SubscriptionList)
		{
		Subscription toAdd = new Subscription(c, r, id,relay);
		if(relay)
		{
			ServerSubscription temp;
			for(ObjectServer s:ServerList)
			{
				temp = new ServerSubscription(s, r,c,id);
				toAdd.getSubscribedServers().add(temp);
				temp.start();
			}
		}
		SubscriptionList.add(toAdd);
		}
		response = query(false, r);
		if(response.getResponse().equals("error"))
		{
			response = new Response(true, null);
		}
		response.setId(id);
//		System.out.println("Leaving subscribe method");
		return response;
	}
	public void addtoSubscribe(Subscription s,ObjectServer toAdd){
		ServerSubscription temp=new ServerSubscription(toAdd,s.getResourceTemplate(),s.getConnection(),s.getId());
		s.getSubscribedServers().add(temp);
		temp.start();
	}
	/* Remove an item from the subscription list. 
	 * @param c - the connection object for the client
	 * @param id - the id of the subscription by the client
	 * @return - the response on successful deletion
	 */
	public void UnSubscribe(Connection c, String id) throws IOException
	{
		Response response;
		synchronized(SubscriptionList)
		{
			removeFromSubscriptionList(c,id);
			boolean closeConnection = closeConnection(c);
			if(closeConnection)
			{
				JSONObject temp = new JSONObject();
				temp.put("resultSize", c.getHits());
				
				try {
					c.sendJSON(temp);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	/*
	 * 
	 */
	public void sendSubscribe(ResourceServer toCheck)
	{
		for(int i=0;i<SubscriptionList.size();i++)
		{
			System.out.println("Iterating Subscription List:"+i);
			if(matchResources(SubscriptionList.get(i).getResourceTemplate(),toCheck)){
				try {
					System.out.println("Matched with connection:"+SubscriptionList.get(i));
					toCheck.setOwner("*");
					SubscriptionList.get(i).getConnection().sendResource(toCheck);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	void removeFromSubscriptionList(Connection c, String id) throws IOException
	{
		synchronized(SubscriptionList)
		{
		for(int i=0; i<SubscriptionList.size();i++)
		{
			System.out.println("id:"+SubscriptionList.get(i).getId());
			if(SubscriptionList.get(i).getConnection()==c 
					&& SubscriptionList.get(i).getId().equals(id))
			{
				for(ServerSubscription s:SubscriptionList.get(i).getSubscribedServers())
				{
					s.Unsubscribe();
				}
				SubscriptionList.remove(i);
			}
		}
		}
	}
	boolean closeConnection(Connection c)
	{
		boolean closeConnection = true;
		for(int i=0; i<SubscriptionList.size();i++)
		{
			if(SubscriptionList.get(i).getConnection()==c)
			{
				closeConnection = false;
			}
		}
		return closeConnection;
	}
	public Response remove(ResourceServer toRemove)
	{
		String key=toRemove.getOwner()+toRemove.getChannel()+toRemove.getUri();
		Response response;
		//Response response
		if(ResourceList.containsKey(key)){
				ResourceList.remove(key);
				response=new Response(true, null);
			}
		else{
			response = new Response(false,"cannot remove resource");
		}
			
		return response;

	}


	public Response publish(ResourceServer res) throws URISyntaxException{	
//	      if (resourceMissing(res)) {
//		    return new Response(false, "missing resource");
//	      }
//	      
	      if (uriInvalid(res)){
		    return new Response(false, "invalid resource");
	      }
	      
	      if (uriIncorrect(res, "PUBLISH") || invalidChannelOwner(res)){
		    return new Response(false , "cannot publish resource");
	      }
	      
	      if (resourceInvalid(res)){
		    return new Response(false, "invalid resource");
	      }
	      
	      if (duplicateResource(res)) {
		    return new Response(true, null);
	      }else{
		    ResourceList.put(res.getOwner()+res.getChannel()+res.getUri(),res);
		    return new Response(true,null);
	      }
	}
	
	public boolean resourceMissing(ResourceServer res){
	      return false;
	}
	
	public boolean uriInvalid(ResourceServer res){
	      try{
		    @SuppressWarnings("unused")
		  URI checkUri = new URI(res.getUri());
//		    System.out.println(checkUri.getScheme());
//		    if (checkUri.getScheme().equals("file")) return true;
		    return false;
	      }catch(URISyntaxException e){
		    return true;
	      }
	}
	
	public boolean uriIncorrect(ResourceServer res, String cmd){
	      try {
		    
		  URI checkUri = new URI(res.getUri());
		  if ( checkUri.isAbsolute() == false){
			return true;
		  }
		  if (cmd.equals("PUBLISH")){
			if (checkUri.getScheme().equals("file")) return true;
			return false;
		  }else{
			if (!checkUri.getScheme().equals("file")) return true;
			return false;
		  }
	    } catch (URISyntaxException e) {
			 return true; // should never happen
	    }
	     
	}
	
	public boolean resourceInvalid(ResourceServer res){
	      if ( (res.getUri().equals("")) ||
	      (res.getEzServer()==null) ||
	      (res.getOwner().equals("*")) ){
		    return true;
	      }
	      return false;
	      
	}
	

	public boolean invalidChannelOwner(ResourceServer res){
	      String OCU = res.getOwner()+res.getChannel()+res.getUri();
	      String CU = res.getChannel()+res.getUri();
	      
//	      System.out.println("OCU " +OCU);
//	      System.out.println("CU " + CU);
	      
	      List<String> l = new ArrayList<String>(ResourceList.keySet());
	      for(String listItem : l){
		    if(listItem.contains(CU)){		//contain channel and uri
//			  System.out.println("contain channel  owner");
//			  System.out.println("Res List: " +listItem);
			  if(!listItem.contains(OCU)){	//not the same owner, rule broken
				return true; // if same resource, different owner,
					     // restrict publishing
			  }
		    }
	      }
	      return false;
	}
	
	public boolean duplicateResource(ResourceServer res){
	      String key = res.getOwner()+res.getChannel()+res.getUri();
	      List<String> l = new ArrayList<String>(ResourceList.keySet());
	      for(String listItem : l){
		    if(listItem.contains(key)){		//contain channel and uri
			  ResourceList.replace(key, res);
			  return true;
		    }
	      }
	      return false;
	}

	public Response share(String secret, ResourceServer res) throws URISyntaxException
	{	
	      if (incorrectSecret(secret)){
		    return new Response(false, "incorrect secret");
	      }
	      
	      if (uriInvalid(res)){
		    return new Response(false, "invalid resource");
	      }
	      
	      if (uriIncorrect(res, "SHARE") || invalidChannelOwner(res)){
		    return new Response(false , "cannot share resource");
	      }
	      
	      if (resourceInvalid(res)){
		    return new Response(false, "invalid resource");
	      }
	      
	      if (duplicateResource(res)) {
		    return new Response(true, null);
	      }else{
		    ResourceList.put(res.getOwner()+res.getChannel()+res.getUri(),res);
		    return new Response(true,null);
	      }
	}

	public boolean incorrectSecret(String secret){
//		if(Arrays.binarySearch(SecretList, secret) == 0){	//if secret is not in the list
		if(!secret.equals(secretServer)){	//if secret is not in the list
//			printResponse(new Response(false, "incorrect secret"));
			return true;
		}
		return false;
	}
	
	public Response query(Boolean relay, ResourceServer toQuery) throws 
	UnknownHostException,
	IOException, 
	ParseException
	{    	Response queryResponse = null;
	      	ArrayList<ResourceServer> matched = getEntry(ResourceList, toQuery);
		//relay to be implemented properly
		if(relay){
			   // make connection

		      ArrayList<ResourceServer> temp = new ArrayList<ResourceServer>();  
		      for(ObjectServer sv : ServerList){

			    // send query
			    try{
				  temp = relaySend(toQuery, sv);
				  matched.addAll(temp);
			    }catch(IOException | ParseException e )
			    {
				  continue;
			    }
		      }      
            	      if(matched.isEmpty()){
            	    	  queryResponse = new Response(false,"invalid resourceTemplate");
            	      } 
            	      else{
            		    queryResponse = new Response(true, null);
            		    queryResponse.setResourceList(matched);
            	      }	
		}
		if(matched.isEmpty()){
		      queryResponse = new Response(false, "invalid resourceTemplate");
		}else{
		      queryResponse = new Response(true,null);
		      queryResponse.setResourceList(matched);
		}
            		return queryResponse;
	}
	public Response fetch(ResourceServer toFetch, DataOutputStream out) throws  IOException{
		Response toReturn = null;
		URI uri = null;
		try {
			uri = new URI(toFetch.getUri());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			toReturn = new Response(false, "invalid resourceTemplate");
			return toReturn;
		}
		boolean sendFile = uri.isAbsolute(); 
		sendFile = checkForFetch(toFetch);
		if(sendFile)
		{
			String fileName = uri.getPath();
			System.out.println(fileName);
		// Check if file exists
		toReturn = sendFile(fileName, out, toFetch);

		}
		else{

			return new Response(false,"missing resourceTemplate");
			// Throw an error here..
		}
		return toReturn;  
		
	}
	ArrayList<ResourceServer> receiveQueryResources(DataInputStream in, JSONParser tempParser) throws IOException, ParseException
	{
		
	    String data;
	    JSONObject tempResponse = new JSONObject();
	    VerifyRequestObject verifier = new VerifyRequestObject();
	    ArrayList<ResourceServer> toReturn = new ArrayList<ResourceServer>();
	    do{
			data = in.readUTF();
			// Attempt to convert read data to JSON
			tempResponse = (JSONObject) tempParser.parse(data);
			
			
			if (verifier.checkTemplate(tempResponse)){
			      ResourceServer toAdd = new ResourceServer(tempResponse);
			      toAdd.setOwner("*");
			      toReturn.add(toAdd);
			      System.out.println("Resource added to list" + toReturn);
			}
			if (debug){
			System.out.println("Received:"+tempResponse.toString());
			}
		  
		} while (tempResponse.containsKey("resultSize") == false);
	    return toReturn;
	}
    ArrayList<ResourceServer> relaySend(ResourceServer toQuery, ObjectServer sv) throws UnknownHostException, IOException, ParseException
    {
    	ArrayList<ResourceServer> toReturn = new ArrayList<ResourceServer>();
    	JSONObject relayQuery = toQuery.toJSON();
	JSONParser tempParser = new JSONParser();
    	Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;
    String tempHost = sv.getHostname();
    int tempPort = sv.getPort();
    
    clientSocket = new Socket(tempHost, tempPort);
    in = new DataInputStream( clientSocket.getInputStream());
    out = new DataOutputStream( clientSocket.getOutputStream());
    // modify the JSON to take out channel / 
    relayQuery.put("channel","");
    relayQuery.put("owner","");
    JSONObject forRelay = new JSONObject();
    forRelay.put("command","QUERY");
    forRelay.put("relay","false");
    forRelay.put("resourceTemplate",relayQuery);
    out.writeUTF(forRelay.toJSONString());
    if(debug)
    {
    	System.out.println("Sent:"+forRelay.toString());
    }
    // receiving query and see if success
    JSONObject received = (JSONObject) tempParser.parse(in.readUTF());
    
    if(debug)
    {
    	System.out.println("Received:"+received.toString());
    }
    // once received

    if(received.containsKey("response")){
	  if(received.get("response").equals("success")){
		  toReturn = receiveQueryResources(in,tempParser);
	 }
    }
    clientSocket.close();
    return toReturn;

    }
	private Response sendFile(String fileName, DataOutputStream out, ResourceServer toFetch) throws IOException
	{
		
		File f = new File(fileName);
		if(f.exists()){
			JSONObject temp = new Response(true, null).toJSON();
			out.writeUTF(temp.toJSONString());
			if(debug)
			{System.out.println("Sent:"+temp.toJSONString());}
			// Send this back to client so that they know what the file is.
			JSONObject trigger = toFetch.toJSON();
			trigger.put("file_size",f.length());
			try {
				// Send trigger to client
				out.writeUTF(trigger.toJSONString());
				if(debug)
					System.out.println(trigger.toJSONString());
				// Start sending file
				RandomAccessFile byteFile = new RandomAccessFile(f,"r");
				byte[] sendingBuffer = new byte[1024*1024];
				int num;
				boolean send = false;
				// While there are still bytes to send..
				while((num = byteFile.read(sendingBuffer)) > 0){
					System.out.println(num);
					out.write(Arrays.copyOf(sendingBuffer, num));
					send=true;
				}
				if(send){
				trigger = new JSONObject();
				trigger.put("resultSize", "1");
				out.writeUTF(trigger.toJSONString());
				if(debug)
					System.out.println(trigger.toString());
				}
				byteFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		{
			return new Response(false,"File Not found");
		}
		return null;
	}

	public boolean checkForFetch(ResourceServer toFetch){
	      for (Entry<String, ResourceServer> entry : ResourceList.entrySet()){
		    if (entry.getValue().getChannel().equals(toFetch.getChannel()) &&
			entry.getValue().getUri().equals(toFetch.getUri())){
			  return true;
		    }
	      }
	      return false;
	}
	
	 public boolean checkifduplicate(ArrayList<ObjectServer> list, ObjectServer toCheck){
	        for(ObjectServer x:list){
	            if(x.getHostname().equals(toCheck.getHostname())&& x.getPort()==toCheck.getPort()){
	                return true;
	            }
	        }
	        return false;
	    }
		    public Response verifyServerList(JSONObject toCheck){
        Response response=new Response();
        //InetAddress host=null;
        String host;
        String port=null;
        String address;
        InetAddressValidator validator=new InetAddressValidator();
        if(toCheck.containsKey("serverList")){
            JSONArray list= (JSONArray) toCheck.get("serverList");
            JSONObject serv;
            for( int i=0;i<list.size();i++){
                serv=(JSONObject) list.get(i);
                if(!serv.containsKey("hostname") || !serv.containsKey("port")){
                    response=new Response(false,"missing resourceTemplate");
                    return response;
                }
                //host=(InetAddress) serv.get("hostname");
                InetAddress address1 = null;
                host=(String) serv.get("hostname");
                port=(String) serv.get("port");
                //InetAddress add = (String) InetAddress.getByName(host);
                //address=(String) InetAddress.getByName(host);
                try{
                    address1=InetAddress.getByName(host);
                }
                catch (UnknownHostException e){
                    response=new Response(false,"missing resourceTemplate");
                    return response;
                }
                //System.out.println(address1.toString());
                address=(String) serv.get("hostname");
                if(!(port.matches("([0-9])+"))){
                    response=new Response(false,"missing resourceTemplate");
                    return response;
                }
                if(!(validator.isValid(address))){
                    response=new Response(false,"missing resourceTemplate");
                    return response;
                }

            }
        }
        else{
            response=new Response(false,"missing or invalid server list");
            return response;
        }
//        response.setResponse("success");
//        response.setErrorMessage("");
//        return response;
		return response;

    }
		    public Response exchange(JSONArray list)
			{
				//Setting up and initialising variables
				Response response;
		        String host=null;
		        int port=0;
		        ObjectServer serv_to_add=null;
		        for(int i=0;i<list.size();i++){
		            JSONObject x=(JSONObject) list.get(i);//Getting the server object as a JSON
		            host=x.get("hostname").toString();//Extracting the host name
		            port=Integer.parseInt(x.get("port").toString());//Extracting the port
		            serv_to_add=new ObjectServer(host,port);//Converting to a Server object
		            if(!(checkifduplicate(ServerList,serv_to_add))){//if server is not already on the list
		                //System.out.println(ServerList.indexOf(serv_to_add));//Debug
		                 ServerList.add(serv_to_add);
		                 for(Subscription s:SubscriptionList){
		                 	if(s.isRelay())
		                 		addtoSubscribe(s,serv_to_add);
						 }

		            }
		        }
		        response=new Response(true, null);
			    return response;
			}
		public void exchange()
				{
			        //Setting up the variables
			//Get a random index
					Random random = new Random();
					int listSize = this.ServerList.size();
					int index = 0;
					if(listSize > 0)//if the serverlist is not empty
					{
			            //Selecting a random server
			            index = random.nextInt(listSize);
					    ObjectServer serv=ServerList.get(index);
			            System.out.println("Selected server: "+serv.getHostname()+":"+serv.getPort());
			            try{	                
			                JSONObject command = ServerListToJSON();
			                sendExchange(command, serv);			                
			        }
					catch (UnknownHostException e){
						ServerList.remove(index);
					}
					catch(IOException e){
						ServerList.remove(index);
					}
					}
					
		}
	// Support method for the Query method
		JSONObject ServerListToJSON()
		{
			//Setting up loop variables and JSON objects to be sent
            JSONObject server;
            JSONObject command=new JSONObject();
            JSONArray servers=new JSONArray();

            //Creating the JSON to send
            command.put("command","EXCHANGE");
            for(ObjectServer x : ServerList){
                server = new JSONObject();
                server.put("hostname",x.getHostname());
                server.put("port",x.getPort());
                servers.add(server);
            }
            command.put("serverList",servers);
            //Sending the JSON
            return command;
		}
		void sendExchange(JSONObject command, ObjectServer serv) throws UnknownHostException, IOException
		{
		    //opening a socket
            Socket socket = new Socket(serv.getHostname(),serv.getPort());
            int timeout=Parameters.EXCHANGE_TIMEOUT;
            socket.setSoTimeout(timeout);
            DataInputStream in = new DataInputStream( socket.getInputStream());
            DataOutputStream out =new DataOutputStream( socket.getOutputStream());
//        		System.out.println("Sending to: "+serv.getHostname()+serv.getPort());//Debug
            out.writeUTF(command.toJSONString());//send
            if(debug)
            {
            	System.out.print("SENT:"+command.toString());
            }
            String data = in.readUTF();   // read a line of data from the stream
//			System.out.println(data);
			socket.close();
           // System.out.println("Exchange socket closed");//Debug	
        }

		  public ArrayList<ResourceServer> getEntry(HashMap<String, ResourceServer> ResourceList,
				  ResourceServer templateResource){
			    // initialize matching array to be return
			    ArrayList<ResourceServer> match = new ArrayList<ResourceServer>();
			    ResourceServer resourcefromList = null;
			    ResourceServer tempResource = null;
			    // Looping through the ResourceList
			    for (Entry<String, ResourceServer> entry : ResourceList.entrySet()){
				  resourcefromList = entry.getValue();
				  if (matchChannel(templateResource,resourcefromList) &&
					      matchOwner(templateResource, resourcefromList) &&
					      matchTags(templateResource, resourcefromList) && 
					      matchURI(templateResource, resourcefromList) &&
					      matchNameDesc(templateResource,resourcefromList))
				  {  
					tempResource = new ResourceServer(resourcefromList);
					tempResource.setOwner("*");
					match.add(tempResource);
//					System.out.println("MATCHED one resource, adding...");//debug
				  }

			    }
			    return match;
		    }

	    
	    
		// Support method for the Query method
			public boolean matchChannel(ResourceServer res1, ResourceServer res2){  
				if (res1.getChannel().equals("")){
//					System.out.println("Channel Matched null");
					return true;
				}
			      if (res1.getChannel().equals(res2.getChannel())){
//				    System.out.println("ChannelMatched");
				    return true;
			      }
			      return false;
			}
			
			public boolean matchOwner(ResourceServer res1, ResourceServer res2){
				if (res1.getOwner().equals("")) {
//					System.out.println("Owner Matched null");
					return true;
							}
				if (res1.getOwner().equals(res2.getOwner())){
//				    System.out.println("Owner matched");
				    return true;
			      }
			      
			      return false;
			}
			
			// have to check that res1 tags are all in res2 tags
			// or res2 have all the tags res1 have
			public boolean matchTags(ResourceServer res1, ResourceServer res2){
			      ArrayList<String> tagList1 = res1.getTags();
			      
			      if(tagList1.isEmpty()) return true;
			      
			      ArrayList<String> tagList2 = res2.getTags();
//			      System.out.println("resource 1 is (the templateResource)" + res1.getTags().toString());
//			      System.out.println("resource 2 is (the list resource)" + res2.getTags().toString());
//			      System.out.println(tagList1);
//			      System.out.println(tagList2);
			      
			      boolean match = false;
			      for (String tag : tagList1){
			    	  match = false;
					   for (String listString : tagList2){
					  if (StringUtils.equalsIgnoreCase(tag, listString)) {
						  match = true;
					  }
				    }
				    if(match)
				    {
				    	return true;
				    }
				    else 
				    	{
				    	return false;
				    	}
			      }
//			      System.out.println("All Tags matches");
			      return true;
			}
			
			
			public boolean matchURI(ResourceServer template, ResourceServer res){
			      if(template.getUri().equals(""))
			    	  {
//			    	  System.out.println("URI matched:null");
			    	  return true;	      
			    	  }
			      String uriTemplate = template.getUri();
			      String resTemplate = res.getUri();	
			      
			      if (uriTemplate.equals(resTemplate)){
//				    System.out.println("URI matched");
				    return true;
			      }
			      return false;
			}
			

			
			public boolean matchNameDesc(ResourceServer template, ResourceServer res){
			      String templateName = template.getName();
			      String resName = res.getName();
			      String templateDesc = template.getDescription();
			      String resDesc = res.getDescription();

			      if(templateName.equals("") && templateDesc.equals("")){
//			    	  System.out.println("name and description are null");
				    return true;
			      }
			      
			      if ((!templateName.equals("")) && 
					  StringUtils.containsIgnoreCase(resName,templateName)){
				    return true;
			      }
			      if ((!templateDesc.equals("")) &&
					  StringUtils.containsIgnoreCase(resDesc,templateDesc)){
				    return true;
			      }
			      return false;
			}
			
			public boolean matchResources(ResourceServer template, ResourceServer b)
			{
				if (matchChannel(template,b) &&
					      matchOwner(template, b) &&
					      matchTags(template, b) && 
					      matchURI(template, b) &&
					      matchNameDesc(template,b)){
					return true;
				}
				else{
					return false;					
				}
			}
	
void printResourceList()
{
    System.out.println("Below is the current resource list");
    
	for (Map.Entry <String,ResourceServer> entry : ResourceList.entrySet()){
		String key = entry.getKey();
		ResourceServer r = entry.getValue();
		System.out.println("Key is " + key);
		System.out.println("Resource is " + r.toJSON().toString());
	}
			
}

}

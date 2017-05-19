package Server;

import org.json.simple.JSONObject;

public class VerifyRequestObject {
	boolean existsCommand(JSONObject command)
	{
		return (command.containsKey("command"));
	}
	Response checkResourceTemplate(JSONObject command){
		Response response = null;
		JSONObject resourceTemplate = (JSONObject) command.get("resourceTemplate");
		if (command.containsKey("resourceTemplate") == false) {
		      response = new Response (false,"Missing resourceTemplate");
		}
		if (checkTemplate(resourceTemplate) == false){
		      // if it gets inside this loop,
		      // then resourceTemplate is wrong
		      response = new Response(false,"invalid resourceTemplate");
		      // use above response to parse into JSON and .. yeah.. 
		}
		return response;
		
	}
	public boolean checkTemplate(JSONObject toCheck){
	      // check for all of the key and see if they are all intact
	      boolean complete = true;
	      String[] checkList = {"name","tags","description","uri","channel","owner","ezserver"};
	      for (int i = 0; i < checkList.length ; ++i){
		    if( !toCheck.containsKey(checkList[i]) ) {
			  complete = false;
		    }
	      }
	      // if complete remains true, it's intact
	      return complete;
	}
	public boolean checkResource(JSONObject inputResource, String cmd)
	{
		boolean convert;
		switch(cmd)
		{
		case "SHARE":
			convert = inputResource.containsKey("resource");
			convert = inputResource.containsKey("secret");
			break;
		case "REMOVE":
		case "PUBLISH":
			convert = inputResource.containsKey("resource");
			break;
		case "QUERY":
		case "FETCH":
		case "SUBSCRIBE":
			convert = inputResource.containsKey("resourceTemplate");
			break;
		case "EXCHANGE":
			convert = inputResource.containsKey("serverList");
			break;
		case "UNSUBSCRIBE":
			convert = inputResource.containsKey("id");
			break;
		default:
			convert = false;
			break;
		}
		
		return convert;
	}
	Response getMissingResponse(String cmdText)
	{
		Response error;
		if(cmdText.equals("PUBLISH")|| cmdText.equals("SHARE") || cmdText.equals("REMOVE")
				|| cmdText.equals("SUBSCRIBE")){
			error = new Response(false, "missing resource");
			
		}
		else if(cmdText.equals("QUERY")|| cmdText.equals("FETCH")){
			error = new Response(false, "missing resourceTemplate");
		}
		else if(cmdText.equals("EXCHANGE"))
		{
			error = new Response(false, "missing resourceTemplate");
		}
		else
		{
			error = new Response(false, "invalid command");
		}
		return error;
	}


}

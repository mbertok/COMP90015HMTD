package Server;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Response {
	private String response;
	private String errorMessage;
	private String id;
	private ArrayList<ResourceServer> responseList;
	public Response(boolean response, String errorMessage)
	{
		if(response == true)
			this.response = "success";
		else
			this.response = "error";
		this.errorMessage = errorMessage;
		this.responseList = new ArrayList<ResourceServer>();
	}
	public Response()
	{
		this.response = null;
		this.errorMessage = null;
		this.responseList = new ArrayList<ResourceServer>();
	}
	public ArrayList<ResourceServer> getResourceList(){
	      return new ArrayList<ResourceServer>(responseList); // new copy
	}
	
	public void setResourceList(ArrayList<ResourceServer> responseList){
	      this.responseList = responseList;
	}
	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}
	/**
	 * @param response the response to set
	 */
	public void setResponse(String response) {
		this.response = response;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public JSONObject toJSON(){
	    JSONObject reply = new JSONObject();
	    JSONObject resourceObject = null;
	    JSONArray resources = new JSONArray();
	    switch(response)
	    {
	    case "success": 
	    	reply.put("response", "success");
	    	break;
	    case "error":
	    	reply.put("response", "error");
	    	reply.put("message", this.errorMessage);
	    break;
	    case "default":
	    	break;
	    }
		return reply;
	}
	public boolean responseListIsEmpty()
	{
		return responseList.isEmpty();
	}
	public ArrayList<JSONObject>getResponseListToJSON()
	{
		ArrayList<JSONObject> jsonResponseList = new ArrayList<JSONObject>();
		if(responseList.isEmpty() == false)
    	{
    		int iterator = 0;
    		for(ResourceServer s:responseList)
    		{
    			iterator++;
    			JSONObject tempObject = s.toJSON();
    			jsonResponseList.add(tempObject);
    		}
    	}

		return jsonResponseList;
		
	}
	public void printList()
	{
		for(ResourceServer s:responseList)
		{
			System.out.println(s.toString());
		}
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}

package Client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Server.ObjectServer;

public class ResourceClient {
	private String Name;
	private String Description;
	private String[] Tags;
	private String uri;
	private String Channel;
	private String Owner;
	private ObjectServer EzServer;
	
	public ResourceClient(String owner, String channel, String uri)
	{
		this.Name = null;
		this.Description = null;
		this.Tags = null;
		this.EzServer = null;
		this.uri = uri;
		if(channel == null)
			this.Channel = "";
		if(owner == null)
			this.Owner = "";
		
	}
	public ResourceClient(String name, String[] tags,
		    String description,
		    String uri, String channel, 
		    String owner, ObjectServer ezserver){
	      this.Name = name;
	      this.Tags = tags;
	      this.Description = description;
	      this.uri = uri;
	      this.Channel = channel;
	      this.Owner = owner;
	      this.EzServer = ezserver;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return Name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		Name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return Description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		Description = description;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the tags
	 */
	public String[] getTags() {
		return Tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(String[] tags) {
		Tags = tags;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return Channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		Channel = channel;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return Owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		Owner = owner;
	}

	/**
	 * @return the ezServer
	 */
	public ObjectServer getEzServer() {
		return EzServer;
	}

	/**
	 * @param ezServer the ezServer to set
	 */
	public void setEzServer(ObjectServer ezServer) {
		EzServer = ezServer;
	}
	public JSONObject toJSON(){
		JSONObject resource = new JSONObject();
	    JSONArray tags = new JSONArray();
	    tags = tagsToArrayNode();
        resource.put("name", this.Name);
        resource.put("tags", tags);
        resource.put("description", this.Description);
        resource.put("uri", this.uri);
        resource.put("channel", this.Channel);
        resource.put("owner", this.Owner);
        resource.put("ezserver", this.EzServer.toString());
		return resource;
	}
	private JSONArray tagsToArrayNode(){
		JSONArray tags = new JSONArray();
		if(this.Tags == null)
		{
			tags.add(" ");
		}
		else{
			for(String x:Tags){
	        	tags.add(x);
	        }			
		}

		return tags;
	}

}

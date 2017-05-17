package Server;

import org.json.simple.JSONObject;

public class ObjectServer {
	private String hostName;
	private int port;
	public ObjectServer(String hostname, int port)
	{
		this.hostName = hostname;
		this.port =  port;
	}
	public ObjectServer(JSONObject toConvert)
	{
		toConvert.get("");
	}
	/**
	 * @return the hostName
	 */
	public String getHostname() {
		return hostName;
	}
	/**
	 * @param hostname the hostName to set
	 */
	public void setHostname(String hostname) {
		this.hostName = hostname;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	public String getPortString()
	{
		return Integer.toString(port);
	}
	public String toString(){
		return this.hostName+":"+Integer.toString(this.port);
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
}

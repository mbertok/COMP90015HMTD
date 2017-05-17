package Client;

public class ObjectServer {
	private String hostName;
	private int port;
	public ObjectServer(String hostname, int port)
	{
		this.hostName = hostname;
		this.port =  port;
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
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	public String toString(){
		return this.hostName+":"+Integer.toString(this.port);
	}
}

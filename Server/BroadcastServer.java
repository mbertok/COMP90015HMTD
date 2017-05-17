package Server;

public class BroadcastServer {
	static String servername;
	static int port;
	BroadcastServer(String servername, int port)
	{
		this.servername = servername;
		this.port = port;
	}
	String getBroadcastServer()
	{
		return (servername+":"+port);
	}

}

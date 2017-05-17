package Server;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import org.json.simple.JSONObject;

public class RequestCheck {
	private long connectionInterval;
	private HashMap<InetAddress,Long> connectedClients;

	RequestCheck(int interval)
	{
		connectedClients = new HashMap<InetAddress,Long>();
		connectionInterval = interval;
	}
	public boolean verifyClient(Socket clientSocket)
	{
		boolean verification;
		InetAddress tempIP = clientSocket.getInetAddress();
		
		if(connectedClients.containsKey(tempIP))
		{
			long lastConnectionTime = connectedClients.get(tempIP);
			verification = verifyDelay(lastConnectionTime, tempIP);
		}
		else
		{
			connectedClients.put(tempIP, System.currentTimeMillis());
			verification = true;
		}
		return verification;
	}
	boolean verifyDelay(long lastConnectionTime, InetAddress tempIP)
	{
		long delay = (System.currentTimeMillis() - lastConnectionTime);
		if(delay >= connectionInterval)
		{
			connectedClients.replace(tempIP, System.currentTimeMillis());
			return true;
		}
		else
		{
			return false;
		}
	}
	void periodicRemove()
	{
		long lastConnectionTime;
		boolean verification;
		
		for (InetAddress tempIP : this.connectedClients.keySet()) {
		if(connectedClients.containsKey(tempIP))
		{
			lastConnectionTime = connectedClients.get(tempIP);
			verification = verifyDelay(lastConnectionTime, tempIP);
			if(verification)
			{
				connectedClients.remove(tempIP);
			}
			
		}
		
		}
		
	}



}

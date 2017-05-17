package Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

public class ServerCommands {
	private InetAddress advertisedHostName;
	private int connectionInterval;
	private int exchangeInterval;
	private int port;
	private String secret;
	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}
	/**
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}
	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}
	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	boolean debug;
	
	public ServerCommands(String advertisedHostName, int connectionInterval, int exchangeInterval,
			int port, String secret, boolean debug){
		this.setAdvertisedHostName(advertisedHostName);
		this.setConnectionInterval(connectionInterval);
		this.setExchangeInterval(exchangeInterval);
		this.setPort(port);
		this.secret = secret;
		this.debug = debug;
		
		
	}
	ServerCommands(){
		Random portrandom = new Random();
		this.setAdvertisedHostName(null);
		this.setConnectionInterval(1000);
		this.setExchangeInterval(600000);
		this.setPort(portrandom.nextInt(65000));
		this.secret = "abc1234";
		this.debug = false;
	}
	/**
	 * @return the advertisedHostName
	 */
	public InetAddress getAdvertisedHostName() {
		return advertisedHostName;
	}
	/**
	 * @param advertisedHostName the advertisedHostName to set
	 */
	public void setAdvertisedHostName(String advertisedHostName) {
		try {
			this.advertisedHostName = InetAddress.getByName(advertisedHostName);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Hostname not found. Using default hostname.");
		}
	}
	/**
	 * @return the connectionInterval
	 */
	public int getConnectionInterval() {
		return connectionInterval;
	}
	/**
	 * @param connectionInterval the connectionInterval to set
	 */
	public void setConnectionInterval(int connectionInterval) {
		this.connectionInterval = connectionInterval;
	}
	/**
	 * @return the exchangeInterval
	 */
	public int getExchangeInterval() {
		return exchangeInterval;
	}
	/**
	 * @param exchangeInterval the exchangeInterval to set
	 */
	public void setExchangeInterval(int exchangeInterval) {
		this.exchangeInterval = exchangeInterval;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

}

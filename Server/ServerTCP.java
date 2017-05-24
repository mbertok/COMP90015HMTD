package Server;
import java.net.*;
import java.io.*;
import java.util.Timer;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
public class ServerTCP extends Thread {
	int insecureServerPort; // the unsecure server port
	int secureServerPort; 
	ServerSocket insecurelistenSocket;
	SSLServerSocket secureServerSocket;
	Services TCPService;
	PeriodicExchange exchange;
	PeriodicRemove remove;
	Timer TimedExchange;
	RequestCheck checkRequest;
	int exchangeInterval;
	int connectionInterval;
	InetAddress hostname;
	boolean debug;
	String secret;
	boolean isSecure;
	public ServerTCP(ServerCommands command, boolean isSecure)
	{
		this.isSecure = isSecure;
		debug = command.debug;
		exchangeInterval = command.getExchangeInterval();
		connectionInterval = command.getConnectionInterval();
		secret = command.getSecret();
		hostname = command.getAdvertisedHostName();
		insecureServerPort = command.getPort();
		secureServerPort = command.getsPort();
		TCPService = new Services(secret);
		exchange = new PeriodicExchange(TCPService);
		TimedExchange = new Timer();
		checkRequest = new RequestCheck(connectionInterval);
		remove = new PeriodicRemove(checkRequest);
		if(this.isSecure)
		{
			System.setProperty("javax.net.ssl.trustStore", "serverKeystore/serverTruststore.jks");  
			System.setProperty("javax.net.ssl.trustStorePassword","comp90015");   
			System.setProperty("javax.net.ssl.keyStore","serverKeystore/serverKeystore.jks");
			System.setProperty("javax.net.ssl.keyStorePassword","comp90015");	
		}

	}
	public void run()
	{

		TimedExchange.scheduleAtFixedRate(remove, Parameters.PERIODIC_REMOVE_START_DELAY, Parameters.PERIODIC_REMOVE_INTERVAL);
		TimedExchange.scheduleAtFixedRate(exchange, Parameters.EXCHANGE_START_DELAY, exchangeInterval);
		try
		{
			if(isSecure)
			{
			//////* SSL
				SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
						.getDefault();
				secureServerSocket = (SSLServerSocket) sslserversocketfactory.createServerSocket();
			    InetSocketAddress endpoint = new InetSocketAddress(hostname, secureServerPort);
			    secureServerSocket.bind(endpoint);
				BroadcastServer tempS = 
				new BroadcastServer(secureServerSocket.getInetAddress().toString(),secureServerSocket.getLocalPort());
				System.out.println("SSL Secure Server Running");
			////// SSL	*/
				System.out.println(tempS.getBroadcastServer());
				System.out.println("Secret:"+secret);
				while(true) {
					//Accept client connection
					SSLSocket sslClientSocket = (SSLSocket) secureServerSocket.accept();
					System.out.println("ServerTCP-start: SSLClientSocket connected");
	//////* SSL:change	sslclientsocket = clientSocket
					synchronized(checkRequest)
					{
					if(checkRequest.verifyClient(sslClientSocket))
					{
						Connection c = new Connection(sslClientSocket, TCPService, debug);
						c.start();
					}
					}
				}
	////// SSL	*/	

			}
			else if(isSecure == false)
			{
			insecurelistenSocket = new ServerSocket(insecureServerPort, 0 , hostname);
			BroadcastServer temp = 
			new BroadcastServer(insecurelistenSocket.getInetAddress().toString(),insecurelistenSocket.getLocalPort());
			System.out.println("Insecure Server Running");
			System.out.println(temp.getBroadcastServer());
			System.out.println("Secret:"+secret);
			while(true) {
				Socket clientSocket = insecurelistenSocket.accept();
				synchronized(checkRequest)
				{
				if(checkRequest.verifyClient(clientSocket))
				{
					Connection c = new Connection(clientSocket, TCPService, debug);
					c.start();
				}
				}
			}
			}
		}
		catch(IOException e)
		{
	       System.out.println("Listen socket:"+e.getMessage());
	       }
		}
	}

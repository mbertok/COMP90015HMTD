package Server;
import java.net.*;
import java.io.*;
import java.util.Timer;
public class ServerTCP {
	int serverPort; // the server port
	ServerSocket listenSocket;
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
	public ServerTCP(ServerCommands command)
	{
		debug = command.debug;
		exchangeInterval = command.getExchangeInterval();
		connectionInterval = command.getConnectionInterval();
		secret = command.getSecret();
		hostname = command.getAdvertisedHostName();
		serverPort = command.getPort();
		TCPService = new Services(secret);
		exchange = new PeriodicExchange(TCPService);
		TimedExchange = new Timer();
		checkRequest = new RequestCheck(connectionInterval);
		remove = new PeriodicRemove(checkRequest);
	}
	public void start()
	{

		TimedExchange.scheduleAtFixedRate(remove, Parameters.PERIODIC_REMOVE_START_DELAY, Parameters.PERIODIC_REMOVE_INTERVAL);
		TimedExchange.scheduleAtFixedRate(exchange, Parameters.EXCHANGE_START_DELAY, exchangeInterval);
		try
		{
			listenSocket = new ServerSocket(serverPort, 0 , hostname);
			BroadcastServer temp = 
			new BroadcastServer(listenSocket.getInetAddress().toString(),listenSocket.getLocalPort());
			System.out.println("Server Running");
			System.out.println(temp.getBroadcastServer());
			System.out.println("Secret:"+secret);
			while(true) {
				Socket clientSocket = listenSocket.accept();
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
		catch(IOException e)
		{
	       System.out.println("Listen socket:"+e.getMessage());
	       }
		}
	}

package Server;
import java.util.TimerTask;
public class PeriodicExchange extends TimerTask{
	Services TCPService;
	public PeriodicExchange(Services s)
	{
		TCPService = s;
	}
	public void run()
	{
//		System.out.println("Exchange Executing");
		synchronized(TCPService){
		TCPService.exchange();
		}
//		System.out.println("Exchange Executed");
	}
}

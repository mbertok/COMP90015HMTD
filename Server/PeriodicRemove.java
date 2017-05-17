package Server;

import java.util.TimerTask;

public class PeriodicRemove extends TimerTask {
	private RequestCheck checkRequest;
	public PeriodicRemove(RequestCheck checkRequest)
	{
		this.checkRequest = checkRequest;
	}
	public void run()
	{
	
//		System.out.println("Remove Executing");
		synchronized(checkRequest){
			checkRequest.periodicRemove();
		}
//		System.out.println("Remove Executed");
	}

}

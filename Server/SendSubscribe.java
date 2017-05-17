package Server;

public class SendSubscribe extends Thread {
	Services availableServices;
	ResourceServer toCheck;
	SendSubscribe(Services aS, ResourceServer toCheck)
	{
		this.availableServices = aS;
		this.toCheck = toCheck;
	}
	public void run()
	{
		availableServices.sendSubscribe(toCheck);
	}
}

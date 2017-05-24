package Server;
public class Server {
	static ServerArgumentParser commandParser;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
		commandParser = new ServerArgumentParser(args);
		commandParser.parseInput();
		ServerCommands initialCommands = commandParser.getCommands();
		ServerTCP unSecureServer = new ServerTCP(initialCommands,false);
		unSecureServer.start();
		ServerTCP secureServer = new ServerTCP(initialCommands,true);
		secureServer.start();
		}catch(NumberFormatException e){
			System.out.println("Invalid Input!");
		}
	}


}

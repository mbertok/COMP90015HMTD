package Client;//package Client;
//import java.net.*;
//import java.io.*;
//
//public class clientQuery {
//      int serverPort;
//      Socket ClientSocket;
//      String message;
//      
//      clientQuery(int port, String inpmessage){
//	    serverPort = port;
//	    this.message = inpmessage;
//      }
//      
//      public void run(){
//	    try{
//		  // localhost needed to be parsed
//		  ClientSocket = new Socket("localhost",serverPort);
//		  
//		  System.out.println("Connection established");
//		  DataInputStream in = new DataInputStream( ClientSocket.getInputStream());
//		  DataOutputStream out = new DataOutputStream( ClientSocket.getOutputStream());
//		  
//		  System.out.println("Sending query");
//		  
//		  
//	    }
//      }
//      
//      
//      
//
//}

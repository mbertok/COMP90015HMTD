package Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.json.simple.JSONObject;

public class GetInput extends Thread {
	DataOutputStream out;
	String id;
	public GetInput(DataOutputStream out, String id)
	{
		this.out = out;
		this.id = id;
	}
	public void run()
	{
		
		Scanner  userInput = new Scanner(System.in);
		System.out.println("Running a new thread for input");
		while(userInput.hasNextLine()==false);
		
		System.out.println("Sending unsubscribe");
		JSONObject temp = new JSONObject();
		temp.put("command", "UNSUBSCRIBE");
		temp.put("id", "1");
		try {
			out.writeUTF(temp.toJSONString());
			out.flush();
			System.out.println("SENT:"+temp.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

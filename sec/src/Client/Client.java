package Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Scanner;

import Server.ServerI;

public class Client {
	
	
	static ServerI _stub;
	static  int id;

	private Client() {}

    public static void main(String[] args) {


    String host = (args.length < 1) ? null : args[0];
	Scanner keyboard = new Scanner(System.in);
    try {
	    Registry registry = LocateRegistry.getRegistry(host);
	    _stub = (ServerI) registry.lookup("Hello");
	    
	    String response = _stub.sayHello();
	    System.out.println("response: " + response);
	} catch (Exception e) {
	    System.err.println("Client exception: " + e.toString());
	    e.printStackTrace();
	}
	
	while(true){
		String choice = keyboard.nextLine();
		String[] splited = choice.split(" "); 
		switch(splited[0]){
			case "init":
				try {
					FS_init();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case "read":
				FS_read(splited[1].getBytes(),Integer.parseInt(splited[2]),Integer.parseInt(splited[3]),splited[4].getBytes());
				break;
			case "write":
				FS_write(Integer.parseInt(splited[1]),Integer.parseInt(splited[2]),splited[3].getBytes());
				break;
			default:
				System.out.println("Usage ....");
		}
	}
	
    }
    
    
	public static byte[] FS_init() throws RemoteException{
		byte[] tempId = {23};
		return _stub.get(tempId) ;
		
	}
	
	public static void FS_write(int pos, int size, byte[] contents){
		
	}
	
	public static byte[] FS_read(byte[] id, int pos, int size, byte[] contents ){
		return null;
	}
    
}

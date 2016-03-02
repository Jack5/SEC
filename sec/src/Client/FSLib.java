package Client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import Server.SecureFSInterface;

public class FSLib {
	
	
	static SecureFSInterface _stub;
	
	
	
	
	public static byte[] FS_init(){
		
		 //String host = (args.length < 1) ? null : args[0];
			SecureFSInterface serverInstance = null;
			try {
			    Registry registry = LocateRegistry.getRegistry(1099);
			    SecureFSInterface stub = (SecureFSInterface) registry.lookup("fs.Server");
			    
			} catch (Exception e) {
			    System.err.println("Client exception: " + e.toString());
			    e.printStackTrace();
			}  
			return null;
		
	}
	
	public static void FS_write(int pos, int size, byte[] contents) {
		
		byte[] f = {3};
		try {
			_stub.FS_write(pos, size, contents);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public static int FS_read(byte[] id, int pos, int size, byte[] contents ){
		byte[] f = {3};
		try {
			_stub.FS_read(id, pos, size, contents);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
		
	}
	

	public static void manageInput(String choice) {
		String[] splited = choice.split(" "); 
		switch(splited[0]){
			case "init":
				FSLib.FS_init();
					break;
				case "read":
				FSLib.FS_read(splited[1].getBytes(),Integer.parseInt(splited[2]),Integer.parseInt(splited[3]),splited[4].getBytes());
				break;
			case "write":
				FSLib.FS_write(Integer.parseInt(splited[1]),Integer.parseInt(splited[2]),splited[3].getBytes());
			break;
				case "help":
			default:
				System.out.println("Available Commands:");
				System.out.println("init");
				System.out.println("read id pos size contents");
				System.out.println("write pos size contents");
				break;
			}
	}
		
}
	
	

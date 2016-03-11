package Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import Exceptions.InvalidContentException;
import Exceptions.InvalidSignatureException;
import Server.SecureFSInterface;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private Client() {}

    public static void main(String[] args) {

		String host = (args.length < 1) ? null : args[0];
		
	    Scanner keyboard = new Scanner(System.in);
	    while(true){
	    	String choice = keyboard.nextLine();
	    	try {
				FSLib.manageInput(choice);
			} catch (NumberFormatException | InvalidSignatureException
					| InvalidContentException | Exceptions.InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	    }
	    
    }
    
}
    
	
    


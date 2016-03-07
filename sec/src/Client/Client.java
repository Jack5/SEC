package Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import Server.SecureFSInterface;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private Client() {}

    public static void main(String[] args) {

		String host = (args.length < 1) ? null : args[0];
		
	    Scanner keyboard = new Scanner(System.in);
	    while(true){
		//	String choice = keyboard.nextLine();
	    	//FSLib.manageInput(choice);
	    	System.out.println("AUX");
	    	int aux = keyboard.nextInt();
	    	System.out.println("first");
	    	int firstBlockPos = keyboard.nextInt();
	    	
			for(;aux != firstBlockPos; aux--){
				System.out.println(aux);
			}
	    }
	    
    }
    
}
    
	
    


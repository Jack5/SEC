package Client;

import java.util.Scanner;

import Exceptions.InvalidContentException;
import Exceptions.InvalidSignatureException;
import Exceptions.WrongStorageException;

public class Client {

    private Client() {}

    public static void main(String[] args) {

		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
	    while(true){
	    	String choice = keyboard.nextLine();
	    	try {
				FSLib.manageInput(choice);
			} catch (NumberFormatException | InvalidSignatureException
					| InvalidContentException | Exceptions.InvalidKeyException | WrongStorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	    }
	    
	    
    }
    
}
    
	
    


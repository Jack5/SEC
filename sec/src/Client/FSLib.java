package Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.Scanner;

import Server.Header;
import Server.SecureFSInterface;

public class FSLib {
	
	
	static SecureFSInterface _stub;
	
	static PublicKey pubKey;
	static Signature sigSigner;
	static byte[] id;
	
	public static byte[] FS_init(){
		
		 //String host = (args.length < 1) ? null : args[0];
			SecureFSInterface serverInstance = null;

			try {
			    Registry registry = LocateRegistry.getRegistry(1099);
			    _stub = (SecureFSInterface) registry.lookup("fs.Server");
			    System.out.println("connected");
			    
			    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			    SecureRandom secRand = SecureRandom.getInstanceStrong();
			    keyGen.initialize(2048, secRand);
			    
			    KeyPair pair = keyGen.generateKeyPair();
			    sigSigner  = Signature.getInstance("SHA256withRSA");
			    sigSigner.initSign(pair.getPrivate());
			    
			    pubKey = pair.getPublic();
			    
			    Header newHeader = new Header();
			    byte[] serialized = serialize(newHeader);
			    sigSigner.update(serialized);
			    byte[] signed = sigSigner.sign();
			    id =_stub.put_k(null, signed, pubKey);
			    
			    System.out.println(id);
			    
			} catch (Exception e) {
			    System.err.println("Client exception: " + e.toString());
			    e.printStackTrace();
			}  
			return null;
		
	}
	
	public static void FS_write(int pos, int size, byte[] contents) {	
		try {
			Header savedHeader = (Header) deserialize(_stub.get(id));
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	
	public static int FS_read(byte[] id, int pos, int size, byte[] contents ){
		byte[] f = {3};
		try {
			_stub.get(null);
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
		
	public static byte[] serialize(Object obj) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
            
	    os.writeObject(obj);
	    byte[] outputBytes = out.toByteArray();
	    out.close();
            
	    return outputBytes;
	}
	
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
            
	    Object outputObject = is.readObject();
	    in.close();
            
	    return outputObject;
	}
	
}
	
	

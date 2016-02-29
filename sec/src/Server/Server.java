package Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;


public class Server implements ServerI{
	
	public Server() {}

	    public String sayHello() {
	    	return "Hello, world!";
	    }
		
	    public static void main(String args[]) {
		
		try {
		    Server obj = new Server();
		    ServerI stub = (ServerI) UnicastRemoteObject.exportObject(obj, 0);

		    // Bind the remote object's stub in the registry
		    Registry registry = LocateRegistry.getRegistry();
		    registry.bind("Hello", stub);

		    System.err.println("Server ready");
		} catch (Exception e) {
		    System.err.println("Server exception: " + e.toString());
		    e.printStackTrace();
		}
	    }

		@Override
		public byte[] get(byte[] id) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte[] put_k(byte[] data, byte[] sig, PublicKey pubKey) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte[] put_h(byte[] data) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}


}

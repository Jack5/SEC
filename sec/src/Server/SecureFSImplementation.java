package Server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
	
public class SecureFSImplementation extends UnicastRemoteObject implements SecureFSInterface {
	
    public SecureFSImplementation() throws RemoteException {}

    public static void main(String args[]) {
	
	try {
		LocateRegistry.createRegistry(1099);
		SecureFSImplementation server = new SecureFSImplementation();
		Naming.rebind("fs.Server", server);
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
	public byte[] put_k(byte[] data, Signature sig, PublicKey pubKey) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] put_h(byte[] data) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}



}
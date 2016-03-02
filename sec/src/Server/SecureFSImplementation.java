package Server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
	
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
	public byte[] FS_init() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean FS_write(int pos, int size, byte[] contents) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer FS_read(byte[] id, int pos, int size, byte[] contents) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
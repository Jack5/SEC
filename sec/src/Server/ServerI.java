package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.*;

public interface ServerI extends Remote{
	String sayHello() throws RemoteException;
	
	byte[] get(byte[] id) throws RemoteException;
	byte[] put_k(byte[] data, byte[] sig, PublicKey pubKey)throws RemoteException;
	byte[] put_h(byte[] data) throws RemoteException;
	
	
}

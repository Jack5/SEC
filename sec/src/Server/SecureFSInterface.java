package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Map;
import java.util.Vector;

public interface SecureFSInterface extends Remote {
    
	public byte[] get(String id) throws RemoteException;
	public String put_k(byte[] data, byte[] signed, PublicKey pubKey) throws RemoteException;
	public String put_h(byte[] data) throws RemoteException;
    
}
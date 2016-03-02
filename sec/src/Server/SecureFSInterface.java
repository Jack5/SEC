package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public interface SecureFSInterface extends Remote {
    
	public byte[] get(byte[] id) throws RemoteException;
	public byte[] put_k(byte[] data, Signature sig, PublicKey pubKey) throws RemoteException;
	public byte[] put_h(byte[] data) throws RemoteException;
    
}
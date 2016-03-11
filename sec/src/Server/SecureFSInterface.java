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
	public String put_k(Vector<String> data, byte[] signed, PublicKey pubKey) throws RemoteException;
	public String put_h(byte[] data) throws RemoteException;
	
	
	//TESTING PURPOSES
	void changeIdVector(int pos, String fakeId, String headerToChange) throws RemoteException;
	void changeSignedHash(byte[] fakeSigned, String headerToChange) throws RemoteException;
	void changePublicKey(PublicKey fakePubKey, String headerToChange) throws RemoteException;
	void changeContentBlock(byte[] fakeContent, String contentBlockToChange) throws RemoteException;
	Vector<String> getCBIdsFromHeader(String headerId) throws RemoteException;
    
}
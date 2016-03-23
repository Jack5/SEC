package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Vector;

import javafx.util.Pair;

public interface SecureFSInterface extends Remote {
    
	public byte[] get(String id) throws RemoteException;
	public String put_k(Vector<String> data, byte[] signed, PublicKey pubKey) throws RemoteException;
	public String put_h(byte[] data) throws RemoteException;
	public Vector<Pair<String, Certificate>> readPubKeys() throws RemoteException;
	public boolean storePubKey(Certificate cert, String userName) throws RemoteException;
	
	
	//TESTING PURPOSES
	void changeIdVector(int pos, String fakeId, String headerToChange) throws RemoteException;
	void changeSignedHash(byte[] fakeSigned, String headerToChange) throws RemoteException;
	void changePublicKey(PublicKey fakePubKey, String headerToChange) throws RemoteException;
	void changeContentBlock(byte[] fakeContent, String contentBlockToChange) throws RemoteException;
	Vector<String> getCBIdsFromHeader(String headerId) throws RemoteException;
    
}
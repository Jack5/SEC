package Server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
	
public class SecureFSImplementation extends UnicastRemoteObject implements SecureFSInterface {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7987182049922996428L;
	static Map<byte[],Header> headerBlocks= new HashMap<byte[],Header>();
	static Map<byte[], ContentBlock> allBlocks = new HashMap<byte[], ContentBlock>();
	
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
		Header savedHeader = headerBlocks.get(id);
		byte[] toSend;
		try {
			toSend = serialize(savedHeader);
			return toSend;
		} catch (IOException e) {
			throw new RemoteException("Error on getting data");
		}
	}

	@Override
	public byte[] put_k(byte[] data, byte[] signed, PublicKey pubKey) throws RemoteException {
		byte[] id ;
		
		//Generate ID = hash of public key
		try {
			id = MessageDigest.getInstance("SHA256").digest(pubKey.toString().getBytes());
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			throw new RemoteException("Internal Error");
		}
			
		//Verify received data to be that which was signed
		try {
			Signature sigVerify;
			sigVerify = Signature.getInstance("SHA256withRSA");
			sigVerify.initVerify(pubKey);
			sigVerify.update(data);
			boolean result = sigVerify.verify(signed);
			if(!result){
				throw new RemoteException("Signature Failed - the sent signed and the data do not match!");
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
		//Generate a newHeader from the received data
		try {
			Header newHeader = (Header) deserialize(signed);
			headerBlocks.put(id, newHeader);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		return id;
		
	}

	@Override
	public byte[] put_h(byte[] data) throws RemoteException {
		
		try {
			ContentBlock newContent = (ContentBlock) deserialize(data);
			byte[] hash = 	MessageDigest.getInstance("SHA256").digest(data);
			allBlocks.put(hash, newContent);
		} catch (ClassNotFoundException | IOException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
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
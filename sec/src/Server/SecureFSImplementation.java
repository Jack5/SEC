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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SecureFSImplementation extends UnicastRemoteObject implements SecureFSInterface {

	//test methods
	@Override
	public  void changeIdVector(int pos, String fakeId, String headerToChange)throws RemoteException{
		headerBlocks.get(headerToChange).ids.set(pos, fakeId);
	}
	@Override
	public  void changeSignedHash(byte[] fakeSigned, String headerToChange)throws RemoteException{
		headerBlocks.get(headerToChange).signature = fakeSigned;
	}
	@Override
	public  void changePublicKey(PublicKey fakePubKey, String headerToChange)throws RemoteException{
		headerBlocks.get(headerToChange).pubKey = fakePubKey;
	}
	@Override
	public  void changeContentBlock(byte[] fakeContent, String contentBlockToChange)throws RemoteException{
		contentBlocks.get(contentBlockToChange).content = fakeContent;
	}
	@Override
	public  Vector<String> getCBIdsFromHeader(String headerId)throws RemoteException{
		return headerBlocks.get(headerId).ids;
	}
	

	////////////////////////
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7987182049922996428L;
	static Map<String, Header> headerBlocks= new HashMap<String,Header>();
	static Map<String, ContentBlock> contentBlocks = new HashMap<String, ContentBlock>();

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
	public byte[] get(String id) throws RemoteException {
				
		
		byte[] toSend = null;

		try {
			if(headerBlocks.containsKey(id)){
				Header savedHeader = headerBlocks.get(id);
				toSend = serialize(savedHeader);
			} else if(contentBlocks.containsKey(id)){
				ContentBlock savedBlock = contentBlocks.get(id);
				toSend = serialize(savedBlock);
			}
		} catch (IOException e) {
			throw new RemoteException("Error on getting data");
		}
		return toSend;
	}

	@Override
	public String put_k(Vector<String> data, byte[] signed, PublicKey pubKey) throws RemoteException {
		
		String id ;

		//Generate ID = hash of public key
		try {
			id = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(pubKey.toString().getBytes()));
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			throw new RemoteException("Internal Error");
		}
		
		System.out.println("put_k: " + id);

		//Verify received data to be that which was signed
		try {
			Signature sigVerify;
			sigVerify = Signature.getInstance("SHA256withRSA");
			sigVerify.initVerify(pubKey);
			sigVerify.update(serialize(data));
			boolean result = sigVerify.verify(signed);
			if(!result){
				throw new RemoteException("Signature Failed - the sent signed and the data do not match!");
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Header newHeader = new Header(pubKey, signed, data);
		headerBlocks.put(id, newHeader);

		return id;

	}

	@Override
	public String put_h(byte[] data) throws RemoteException {

		try {
			ContentBlock newContent = new ContentBlock(data);
			String hash = 	Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(data));
			contentBlocks.put(hash, newContent);
			
			System.out.println("put_h: " + hash);
			return hash;
		} catch (NoSuchAlgorithmException e) {
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
package Server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import Exceptions.InvalidSignatureException;
import javafx.util.Pair;

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
	@Override
	public  void cleanCerts()throws RemoteException{
		Enumeration<String> aliases;
		try {
			aliases = keyStore.aliases();
			while(aliases.hasMoreElements()){
				String alias = aliases.nextElement();
				keyStore.deleteEntry(alias);
			}
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	////////////////////////


	/**
	 * 
	 */
	private static final long serialVersionUID = 7987182049922996428L;
	static Map<String, Header> headerBlocks= new HashMap<String,Header>();
	static Map<String, ContentBlock> contentBlocks = new HashMap<String, ContentBlock>();
	private static KeyStore keyStore;

	public SecureFSImplementation() throws RemoteException {}

	public static void main(String args[]) {

		try {
			LocateRegistry.createRegistry(1099);
			SecureFSImplementation server = new SecureFSImplementation();
			Naming.rebind("fs.Server", server);

			//load keystore
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

			// get user password and file input stream
			char[] password = "password".toCharArray();
			keyStore.load(null, password);

			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public boolean storePubKey(Certificate cert, String userName) throws RemoteException{
		boolean result = false;

		try {
			keyStore.setCertificateEntry(userName, cert);
			result = true;
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;		
	}

	@Override
	public Vector<Pair<String, Certificate>> readPubKeys() throws RemoteException{
		Vector<Pair<String, Certificate>> result = new Vector<Pair<String, Certificate>>();
		try {
			Enumeration<String> aliases = keyStore.aliases();
			while(aliases.hasMoreElements()){
				String alias = aliases.nextElement();
				Pair<String, Certificate> pair = new Pair<String, Certificate>(alias, keyStore.getCertificate(alias));
				result.add(pair);
			}
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
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
	public String put_k(Vector<String> data, byte[] signed, PublicKey pubKey) throws RemoteException, InvalidSignatureException {

		String id ;

		//Generate ID = hash of public  key
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
			sigVerify = Signature.getInstance("SHA1withRSA");
			sigVerify.initVerify(pubKey);
			sigVerify.update(serialize(data));
			boolean result = sigVerify.verify(signed);
			if(!result){
				throw new InvalidSignatureException();
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
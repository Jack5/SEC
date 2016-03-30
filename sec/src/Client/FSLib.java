package Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;


import Exceptions.InvalidContentException;
import Exceptions.InvalidKeyException;
import Exceptions.InvalidSignatureException;
import Exceptions.WrongStorageException;
import Server.CCLogic;
import Server.ContentBlock;
import Server.Header;
import Server.SecureFSInterface;
import javafx.util.Pair;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

public class FSLib {


	static SecureFSInterface _stub;

	static KeyPair keyPair;
	static String ownedFileId;
	static X509Certificate cert;
	static ArrayList<X509Certificate> rootCerts;

	//methods for test purposes
	public static PublicKey getPubKey(){
		return keyPair.getPublic();
	}

	public static String getId(){
		return ownedFileId;
	}


	//method for verifying the signature of an header
	private static boolean VerifySignature(Vector<String> data, PublicKey key, byte[] signature){
		//Verify received data to be that which was signed
		boolean result = false;
		try {
			Signature sigVerify;
			sigVerify = Signature.getInstance("SHA1withRSA");
			sigVerify.initVerify(key);
			sigVerify.update(serialize(data));
			result = sigVerify.verify(signature);					
		} catch (NoSuchAlgorithmException | SignatureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (java.security.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return result;
	}

	private static boolean VerifyPublicKey(PublicKey pkey, String id){
		boolean result = false;

		try {
			String computedId = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(pkey.toString().getBytes()));
			result = id.equals(computedId);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	private static boolean VerifyContentBLock(ContentBlock block, String id) {
		boolean result = false;
		try {
			String hash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(block.content));
			result = id.equals(hash);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static byte[] FS_init(){

		try {
			//connect to server
			Registry registry = LocateRegistry.getRegistry(1099);
			_stub = (SecureFSInterface) registry.lookup("fs.Server");
			System.out.println("connected");

			//load cc certificates
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			rootCerts = new ArrayList();
			for(int i = 1; i < 4; i++){
				FileInputStream in = new FileInputStream("C:\\Users\\biscoito\\SEC\\sec\\resources\\Cartao de Cidadao 00" + i + ".cer");
				Certificate c = cf.generateCertificate(in);
				rootCerts.add((X509Certificate) c);
			}

			//init CC
			CCLogic.init();

			ArrayList<X509Certificate> certificates = new ArrayList<X509Certificate>();

			certificates.add(CCLogic.getCertificate(0));
			certificates.add(CCLogic.getCertificate(3));


			CertPath cp = cf.generateCertPath(certificates);

			HashSet<TrustAnchor> anchors = new HashSet<TrustAnchor>();
			for(X509Certificate c : rootCerts) anchors.add(new TrustAnchor(c, null));
			PKIXParameters params = new PKIXParameters(anchors);
			params.setRevocationEnabled(false);
			CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
			PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) cpv.validate(cp, params);

			//get certificate from CC
			cert = CCLogic.getCertificate(0);
			String cardNumber = CCLogic.getCardNumber();

			_stub.storePubKey(cert, cardNumber);

			//empty vector of ids for an uninitialized file
			Vector<String> emptyIds = new Vector<String>();

			//server call
			ownedFileId =_stub.put_k(emptyIds, CCLogic.sign(serialize(emptyIds)), cert.getPublicKey());

			BlockManager.hashEmptyBlock(_stub);
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}  
		return null;

	}

	public static void FS_write(int pos, int size,Buffer buffer) throws InvalidKeyException, InvalidSignatureException, InvalidContentException, WrongStorageException {	
		byte[] contents = buffer.getContent();
		try {
			//get owned file from server
			Header header = getHeader(ownedFileId);

			Vector<String> ids = header.ids;
			int totalFileSize = 0;
			byte[] lastContent = null;

			if(!ids.isEmpty()){
				lastContent = getContentBlock(ids.lastElement()).content;
				totalFileSize = BlockManager.getFileSize(header, lastContent);
			}			

			int[] posModifiedBlocks = BlockManager.getBlockIndices(pos, size);
			int[] posBlockToPad = BlockManager.getBlockIndicesToPad(pos+size-1, totalFileSize);

			Vector<byte[]> newContents = new Vector<byte[]>();

			//padding
			if(posBlockToPad[0] != -1){	
				newContents = BlockManager.addPadding(ids.size() - 1, posModifiedBlocks[posModifiedBlocks.length-1], lastContent, (pos+size-1)%BlockManager.BLOCK_SIZE);

				if(totalFileSize % BlockManager.BLOCK_SIZE == 0)
					ids.add(BlockManager.hashEmpty);
				else
					ids.set(ids.size() - 1,BlockManager.hashEmpty);
				for(int i = 1; i < newContents.size(); i++){

					ids.add(BlockManager.hashEmpty);	
				}	
			}

			Pair<byte[],byte[]> firstLastOriginalBlocks = null;

			if(newContents.isEmpty()){ //there was no padding
				if(!ids.isEmpty()){
					firstLastOriginalBlocks = new Pair<byte[],byte[]>(getContentBlock(ids.get(posModifiedBlocks[0])).content,getContentBlock(ids.get(posModifiedBlocks[posModifiedBlocks.length -1])).content);
				}
				else{
					firstLastOriginalBlocks = new Pair<byte[],byte[]>(new byte[contents.length],new byte[contents.length]);
					newContents.add(firstLastOriginalBlocks.getKey());
					ids.add(BlockManager.hashEmpty);
				}
			}else{ // there was padding
				byte[] firstBlock = null;

				if(posModifiedBlocks[0] == posBlockToPad[0]){ //first block to modify is beyond the EOF and same block
					firstBlock = newContents.get(0);

				}else if(posModifiedBlocks[0] > posBlockToPad[0]){ //first block to modify is beyond the EOF and another block
					int i = 0;
					for(int aux : posBlockToPad){
						if(posModifiedBlocks[0] == aux){
							firstBlock = newContents.get(i);
							break;
						}
						i++;
					}
					ids.set(posBlockToPad[0], _stub.put_h(newContents.get(0)));
				}else { //first block to modify is before EOF	
					firstBlock = getContentBlock(ids.get(posModifiedBlocks[0])).content;
				}
				firstLastOriginalBlocks = new Pair<byte[],byte[]>(firstBlock,newContents.lastElement());
			}

			//ids has all the blocks needed for the writing operations

			Vector<byte[]> finalModifiedBlocks = BlockManager.newBlocks(firstLastOriginalBlocks, pos, contents, posModifiedBlocks.length);

			//insert the new blocks into the header
			int index = posModifiedBlocks[0];
			for(byte[] finalForm : finalModifiedBlocks){
				String id = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(finalForm));
				if(!id.equals(_stub.put_h(finalForm))) throw new WrongStorageException();
				ids.set(index, id);
				index++;		
			}

			_stub.put_k(ids, CCLogic.sign(serialize(ids)), cert.getPublicKey());

		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | PKCS11Exception | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static int FS_read(PublicKey pk, int pos, int size, Buffer buffer ) throws InvalidKeyException, InvalidSignatureException, InvalidContentException{
		String id = null;
		//Generate ID = hash of public  key
		try {
			id = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(pk.toString().getBytes()));
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		//get header from server
		Header header = getHeader(id);

		int currentBlockBeingRead = BlockManager.getBlockByPos(pos);

		if(currentBlockBeingRead > header.ids.size() - 1){
			System.out.println("Initial Pos exceeds EOF!");
			return 0;
		}

		if(currentBlockBeingRead == header.ids.size() -1 && pos%BlockManager.BLOCK_SIZE > header.ids.lastElement().length()){
			System.out.println("Initial Pos exceeds EOF!");
			return 0;
		}



		int bytesRead = 0 ;

		int totalFileSize = BlockManager.getFileSize(header, getContentBlock(header.ids.lastElement()).content);
		int bytesToBeRead = Math.min(totalFileSize - pos , size);
		int lastBlockToRead = BlockManager.getBlockByPos(pos + bytesToBeRead - 1);
		byte[] readBytes = new byte[bytesToBeRead];

		//Read Routines
		byte[] curBlock =  getContentBlock(header.ids.get(currentBlockBeingRead)).content;
		int bytesToReadInFirstBlock = Math.min( curBlock.length - (pos % BlockManager.BLOCK_SIZE), size);
		System.arraycopy(curBlock, pos % BlockManager.BLOCK_SIZE, readBytes, bytesRead, bytesToReadInFirstBlock);
		bytesRead += bytesToReadInFirstBlock;
		currentBlockBeingRead++;

		for(; currentBlockBeingRead < lastBlockToRead ; currentBlockBeingRead++){
			curBlock = getContentBlock(header.ids.get(currentBlockBeingRead)).content;
			System.arraycopy(curBlock, 0, readBytes, bytesRead, BlockManager.BLOCK_SIZE);
			bytesRead += BlockManager.BLOCK_SIZE;
		}

		if(currentBlockBeingRead == lastBlockToRead){
			curBlock = getContentBlock(header.ids.get(currentBlockBeingRead)).content;
			System.arraycopy(curBlock, 0, readBytes, bytesRead, bytesToBeRead - bytesRead);
			bytesRead += bytesToBeRead - bytesRead;
		}

		buffer.setContent(readBytes);			
		return bytesRead;

	}

	//method for retrieving an header from the server that performs integrity checks
	public static Header getHeader(String id) throws InvalidKeyException, InvalidSignatureException{
		Header header = null;

		try {
			header = (Header) deserialize(_stub.get(id));
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/********** integrity checks **************/

		//verify expected pub key: the hash of the received pub key should match the id used to access the file		
		if(!VerifyPublicKey(header.pubKey, id)){
			throw new InvalidKeyException();
		}

		//verify integrity of the header
		if(!VerifySignature(header.ids, header.pubKey, header.signature)){
			throw new InvalidSignatureException();
		}

		return header;
	}

	public static ContentBlock getContentBlock(String id) throws InvalidContentException{
		ContentBlock block = null;

		try {
			block = (ContentBlock) deserialize(_stub.get(id));
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		/********** integrity checks **************/

		//verify received block: the hash of the received block should match the id used to access it	
		if(!VerifyContentBLock(block, id)){
			throw new InvalidContentException();
		}

		return block;
	}

	//TODO parse input better (the content saved in buffer does not allow for spaces)
	public static void manageInput(String choice) throws InvalidKeyException, NumberFormatException, InvalidSignatureException, InvalidContentException, WrongStorageException {
		String[] splited = choice.split(" "); 
		switch(splited[0]){
		case "init":
			FSLib.FS_init();
			break;
		case "read":
			Buffer newBuffer = new Buffer();
			String id = splited[1] + " " + splited[2] + " " + splited[3];
			PublicKey pk = null;
			try {
				for(Pair<String, Certificate> p : _stub.readPubKeys()){
					if(id.equals(p.getKey())) pk = p.getValue().getPublicKey();
				}
			} catch (RemoteException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			int bytesRead  = FSLib.FS_read(pk,Integer.parseInt(splited[4]),Integer.parseInt(splited[5]),newBuffer);
			if(bytesRead != 0){
				System.out.println(new String (newBuffer.getContent()));
				System.out.println("Bytes Read : " + bytesRead);
			}
			break;
		case "write":
			Buffer newBuffer2 = new Buffer();
			newBuffer2.setContent(splited[3].getBytes());
			FSLib.FS_write(Integer.parseInt(splited[1]),Integer.parseInt(splited[2]),newBuffer2);
			break;
		case "dread":
			FSLib.debugRead();
			break;
		case "list":
			try {
				for(Pair<String, Certificate> p : _stub.readPubKeys()){
					System.out.println("user: " + p.getKey());
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "help":
		default:
			System.out.println("Available Commands:");
			System.out.println("init");
			System.out.println("read id pos size ");
			System.out.println("write pos size contents");
			break;
		}
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

	public static void debugRead() throws InvalidKeyException, InvalidSignatureException, InvalidContentException{

		//get header from server
		Header header = getHeader(ownedFileId);

		int count = 0;
		for(String blockId : header.ids){
			ContentBlock block = getContentBlock(blockId);
			System.out.println("block " + count++ + ":\n" + new String(block.content));
		}

	}


	public static void changeIdVector(int pos, String fakeId, String headerToChange){
		try {
			_stub.changeIdVector(pos, fakeId, headerToChange);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void changeSignedHash(byte[] fakeSigned, String headerToChange){
		try {
			_stub.changeSignedHash(fakeSigned, headerToChange);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void changePublicKey(PublicKey fakePubKey, String headerToChange){
		try {
			_stub.changePublicKey(fakePubKey, headerToChange);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void changeContentBlock(byte[] fakeContent, String contentBlockToChange){
		try {



			_stub.changeContentBlock(fakeContent, contentBlockToChange);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Vector<String> getCBIdsFromHeader(String headerId){
		try {
			return _stub.getCBIdsFromHeader(headerId);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}



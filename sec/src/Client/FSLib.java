package Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Vector;

import Server.ContentBlock;
import Server.Header;
import Server.SecureFSInterface;
import javafx.util.Pair;
import jdk.nashorn.internal.ir.BlockStatement;

public class FSLib {


	static SecureFSInterface _stub;

	static KeyPair keyPair;
	static String ownedFileId;
	static final int BLOCK_SIZE = 4096;
	

	//methods for test purposes
	public static PublicKey getPubKey(){
		return keyPair.getPublic();
	}
	
	public static String getId(){
		return ownedFileId;
	}
	

	
	////////////////////////////
	//method for signing the contents of an header
	private static byte[] Sign(Vector<String> ids){
		byte[] serialized = null;
		byte[] signed = null;
		try {
			Signature sigSigner  = Signature.getInstance("SHA256withRSA");
			sigSigner.initSign(keyPair.getPrivate());
			serialized = serialize(ids);
			sigSigner.update(serialized);
			signed = sigSigner.sign();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return signed;
	}

	//method for verifying the signature of an header
	private static boolean VerifySignature(Vector<String> data, PublicKey key, byte[] signature){
		//Verify received data to be that which was signed
		boolean result = false;
		try {
			Signature sigVerify;
			sigVerify = Signature.getInstance("SHA256withRSA");
			sigVerify.initVerify(key);
			sigVerify.update(serialize(data));
			result = sigVerify.verify(signature);					
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
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

			//generate key pair
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			SecureRandom secRand = SecureRandom.getInstanceStrong();
			keyGen.initialize(2048, secRand);
			keyPair = keyGen.generateKeyPair();		    

			//empty vector of ids for an uninitialized file
			Vector<String> emptyIds = new Vector<String>();

			//server call
			ownedFileId =_stub.put_k(emptyIds, Sign(emptyIds), keyPair.getPublic());

			System.out.println("ID associated with this user is:\n" + ownedFileId);
			BlockManager.hashEmptyBlock(_stub);
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}  
		return null;

	}

	public static void FS_write(int pos, int size,Buffer buffer) {	
		byte[] contents = buffer.getContent();
		try {
			//get owned file from server
			Header header = getHeader(ownedFileId);
						
			Vector<String> ids = header.ids;
			int totalFileSize = 0;
			byte[] lastContent = null;
			
			if(!ids.isEmpty()){
				lastContent = ((ContentBlock) deserialize(_stub.get(ids.lastElement()))).content;
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
			}else{
				byte[] firstBlock = null;
				if(posModifiedBlocks[0] >= posBlockToPad[0]){
					int i = 0;
					for(int aux : posBlockToPad){
						if(posModifiedBlocks[0] == aux){
							firstBlock = newContents.get(i);
							break;
						}
						i++;
					}
					 
				}else{
					firstBlock = ((ContentBlock) deserialize(_stub.get(ids.get(posModifiedBlocks[0])))).content;
				}
				firstLastOriginalBlocks = new Pair<byte[],byte[]>(firstBlock,newContents.lastElement());
			}
			
			//ids has all the blocks needed for the writing operations
			
			Vector<byte[]> finalModifiedBlocks = BlockManager.newBlocks(firstLastOriginalBlocks, pos, contents, posModifiedBlocks.length);
			
			//insert the new blocks into the header
			int index = posModifiedBlocks[0];
			for(byte[] finalForm : finalModifiedBlocks){
				if(index > posModifiedBlocks.length){
					
				}
				ids.set(index, _stub.put_h(finalForm));
				
				index++;
				
			}
		
			_stub.put_k(ids, Sign(ids), keyPair.getPublic());
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static int FS_read(String id, int pos, int size, Buffer buffer ){
		try {

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
			
			int totalFileSize = BlockManager.getFileSize(header, ((ContentBlock) deserialize(_stub.get(header.ids.lastElement()))).content);
			int bytesToBeRead = Math.min(totalFileSize - pos , size);
			int lastBlockToRead = BlockManager.getBlockByPos(pos + bytesToBeRead - 1);
			byte[] readBytes = new byte[bytesToBeRead];
			
			//Read Routines
			byte[] curBlock = ((ContentBlock) deserialize(_stub.get(header.ids.get(currentBlockBeingRead)))).content;
			int bytesToReadInFirstBlock = Math.min( curBlock.length - (pos % BlockManager.BLOCK_SIZE), size);
			System.arraycopy(curBlock, pos % BlockManager.BLOCK_SIZE, readBytes, bytesRead, bytesToReadInFirstBlock);
			bytesRead += bytesToReadInFirstBlock;
			currentBlockBeingRead++;
			
			for(; currentBlockBeingRead < lastBlockToRead ; currentBlockBeingRead++){
				curBlock = ((ContentBlock) deserialize(_stub.get(header.ids.get(currentBlockBeingRead)))).content;
				System.arraycopy(curBlock, 0, readBytes, bytesRead, BlockManager.BLOCK_SIZE);
				bytesRead += BlockManager.BLOCK_SIZE;
			}
			
			if(currentBlockBeingRead == lastBlockToRead){
				curBlock = ((ContentBlock) deserialize(_stub.get(header.ids.get(currentBlockBeingRead)))).content;
				System.arraycopy(curBlock, 0, readBytes, bytesRead, bytesToBeRead - bytesRead);
				bytesRead += bytesToBeRead - bytesRead;
			}
			
			buffer.setContent(readBytes);			
			return bytesRead;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	//method for retrieving an header from the server that performs integrity checks
	public static Header getHeader(String id){
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
			//TODO handle wrong public key
			return null;
		}

		//verify integrity of the header
		if(!VerifySignature(header.ids, header.pubKey, header.signature)){
			//TODO handle wrong signature
			return null;
		}
		
		return header;
	}
	
	public static ContentBlock getContentBlock(String id){
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
			//TODO handle wrong content block
			return null;
		}
		
		return block;
	}

	//TODO parse input better (the content saved in buffer does not allow for spaces)
	public static void manageInput(String choice) {
		String[] splited = choice.split(" "); 
		switch(splited[0]){
		case "init":
			FSLib.FS_init();
			break;
		case "read":
			Buffer newBuffer = new Buffer();
			int bytesRead  = FSLib.FS_read(splited[1],Integer.parseInt(splited[2]),Integer.parseInt(splited[3]),newBuffer);
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
			FSLib.debugRead(splited[1]);
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
	
	public static void debugRead(String id){
		
		//get header from server
		Header header = getHeader(id);
		
		int count = 0;
		for(String blockId : header.ids){
			ContentBlock block = getContentBlock(blockId);
			System.out.println("block " + count++ + ":\n" + new String(block.content));
		}
		
	}

}



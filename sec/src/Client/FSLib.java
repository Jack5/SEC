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

		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}  
		return null;

	}

	public static void FS_write(int pos, int size, byte[] contents) {	
		
		try {
			//get owned file from server
			Header header = (Header) deserialize(_stub.get(ownedFileId));

			//********** integrity checks **************

			//verify expected pub key: the hash of the received pub key should match the id used to access the file		
			if(!VerifyPublicKey(header.pubKey, ownedFileId)){
				//TODO handle wrong public key
				return;
			}

			//verify integrity of the header
			if(!VerifySignature(header.ids, header.pubKey, header.signature)){
				//TODO handle wrong signature
				return;
			}
						
			Vector<String> ids = header.ids;
			int totalFileSize = 0;
			byte[] lastContent = null;
			
			if(!ids.isEmpty()){
				lastContent = ((ContentBlock) deserialize(_stub.get(ids.lastElement()))).content;
				totalFileSize = BlockManager.getFileSize(header, lastContent);
			}
			
			
			int[] posModifiedBlocks = BlockManager.getBlockIndices(pos, size);
			int[] posBlockToPad = BlockManager.getBlockIndicesToPad(pos+size, totalFileSize);
			
			Vector<byte[]> newContents = new Vector<byte[]>();
			
			//padding
			if(posBlockToPad[0] != -1){
				System.out.println("need to pad");	
				newContents = BlockManager.addPadding(ids.size() - 1, posModifiedBlocks[posModifiedBlocks.length-1], lastContent, (pos+size)%BlockManager.BLOCK_SIZE);
				
				if(totalFileSize % BlockManager.BLOCK_SIZE == 0)
					ids.add(_stub.put_h(newContents.elementAt(0)));
				else
					ids.set(0, _stub.put_h(newContents.elementAt(0)));
				for(int i = 1; i < newContents.size(); i++){
					System.out.println("ola");
					ids.add(_stub.put_h(newContents.elementAt(i)));	
				}	
			}
			//no need to pad
			else{
				System.out.println("no need to pad");				
			}
			Pair<byte[],byte[]> firstLastOriginalBlocks = null;
			
			if(newContents.isEmpty()){
				firstLastOriginalBlocks = new Pair<byte[],byte[]>(((ContentBlock) deserialize(_stub.get(ids.get(posModifiedBlocks[0])))).content,((ContentBlock) deserialize(_stub.get(ids.get(posModifiedBlocks[posModifiedBlocks.length -1])))).content);
			}else{
				byte[] firstBlock = null;
				if(posModifiedBlocks[0] == posBlockToPad[0]){
					firstBlock = newContents.get(0); 
				}else{
					firstBlock = ((ContentBlock) deserialize(_stub.get(ids.get(posModifiedBlocks[0])))).content;
				}
				firstLastOriginalBlocks = new Pair<byte[],byte[]>(firstBlock,newContents.lastElement());
			}
			
			Vector<byte[]> finalModifiedBlocks = BlockManager.newBlocks(firstLastOriginalBlocks, pos, contents);
			
			/*
			
			if(pos+size > totalFileSize){
				System.out.println("adding new blocks to write");
				newBlocks = BlockManager.splitIntoBlocks(contents, lastContent);
				int i = 0;
				int lastWrittenBlock = ids.size() - 1;
				for(byte[] b : newBlocks){
					System.out.println("adeus");
					if(posModifiedBlocks[i] > lastWrittenBlock){
						System.out.println("add");
						ids.add(_stub.put_h(b));
					}
					else{
						System.out.println("set");
						ids.set(posModifiedBlocks[i], _stub.put_h(b));
					}							
				}
			}
			else{
				//TODO
			}
			*/
			_stub.put_k(ids, Sign(ids), keyPair.getPublic());
			
			/*TEST CODE*/
			Header header2 = (Header) deserialize(_stub.get(ownedFileId));
			
			byte[] lastContent2 = ((ContentBlock) deserialize(_stub.get(header2.ids.lastElement()))).content;
			totalFileSize = BlockManager.getFileSize(header2, lastContent2);
			
			System.out.println("size after: " + totalFileSize );
			
			
			/* SIMPLE CODE TO TEST WRITING TO 2 BLOCKs ALWAYS OVERWRITING PREVIOUS CONTENT */
			
			/*
			int half = contents.length / 2;
			byte[] firstHalf = Arrays.copyOfRange(contents, 0, half);
			byte[] secondHalf = Arrays.copyOfRange(contents, half, contents.length);
			Vector<String> ids2 = new Vector<String>();			
			ids.add(_stub.put_h(firstHalf));
			ids.add(_stub.put_h(secondHalf));
			_stub.put_k(ids2, Sign(ids2), keyPair.getPublic());
			 */

		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static int FS_read(String id, int pos, int size, byte[] contents ){
		try {

			Header header = (Header) deserialize(_stub.get(id));

			//********** integrity checks **************

			//verify expected pub key: the hash of the received pub key should match the id used to access the file		
			if(!VerifyPublicKey(header.pubKey, id)){
				//TODO handle wrong public key
				return -1;
			}

			//verify integrity of the header
			if(!VerifySignature(header.ids, header.pubKey, header.signature)){
				//TODO handle wrong signature
				return -1;
			}

			//TODO read the corresponding bytes
			//current implementation reads all bytes from all blocks
			//?????????? TODO check integrity of each block read ??????????????
			
			// *** pseudo-code ****
			/* 
			 *  vector<string> ids = vector of ids of the content block of the file
			 *  vector<int> ind_blocks_to_read = vector of relative positions of the blocks to be read
			 *  Vector<byte[]> blocks_to_read;
			 *  foreach(string id : ind_blocks_to_read){
			 *  	 blocks_to_read.add(get(ids[id]))
			 *  }
			 *  			 * 
			 *  byte[size] read_bytes = read_blocks(blocks_to_read, pos, size)
			 *  return read_bytes.length
			 */
			
			/* SIMPLE CODE THAT READS ALL CONTENTS OF A FILE */
			int count = 1;
			for(String blockId : header.ids){
				ContentBlock block = (ContentBlock) deserialize(_stub.get(blockId));
				System.out.println("block " + count++ + ":\n" + new String(block.content));
			}
			
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


	public static void manageInput(String choice) {
		String[] splited = choice.split(" "); 
		switch(splited[0]){
		case "init":
			FSLib.FS_init();
			break;
		case "read":
			FSLib.FS_read(splited[1],Integer.parseInt(splited[2]),Integer.parseInt(splited[3]),splited[4].getBytes());
			break;
		case "write":
			FSLib.FS_write(Integer.parseInt(splited[1]),Integer.parseInt(splited[2]),splited[3].getBytes());
			break;
		case "help":
		default:
			System.out.println("Available Commands:");
			System.out.println("init");
			System.out.println("read id pos size contents");
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

}



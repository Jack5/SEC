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
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Vector;

import Server.ContentBlock;
import Server.Header;
import Server.SecureFSInterface;
import javafx.util.Pair;

public class FSLib {
	
	
	static SecureFSInterface _stub;
	
	static KeyPair keyPair;
	static String id;
	static final int BLOCK_SIZE = 4096;
	
	private static Pair<byte[], byte[]> SignAndSerialize(Header header){
		byte[] serialized = null;
		byte[] signed = null;
		try {
			Signature sigSigner  = Signature.getInstance("SHA256withRSA");
			 sigSigner.initSign(keyPair.getPrivate());
			 serialized = serialize(header);
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
		 return new Pair<byte[], byte[]>(signed, serialized);
	}
	
	public static byte[] FS_init(){
		
		 //String host = (args.length < 1) ? null : args[0];
			SecureFSInterface serverInstance = null;

			try {
			    Registry registry = LocateRegistry.getRegistry(1099);
			    _stub = (SecureFSInterface) registry.lookup("fs.Server");
			    System.out.println("connected");
			    
			    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			    SecureRandom secRand = SecureRandom.getInstanceStrong();
			    keyGen.initialize(2048, secRand);
			    
			    keyPair = keyGen.generateKeyPair();		    
			   
			    Header newHeader = new Header();
			    
			    Pair<byte[], byte[]> pair = SignAndSerialize(newHeader);
			    
			    id =_stub.put_k(pair.getValue(), pair.getKey(), keyPair.getPublic());

			    System.out.println(id);
			    
			} catch (Exception e) {
			    System.err.println("Client exception: " + e.toString());
			    e.printStackTrace();
			}  
			return null;
		
	}
	
	public static void FS_write(int pos, int size, byte[] contents) {	
	
		
		try {
			
			Header header = (Header) deserialize(_stub.get(id));
			size = contents.length;
			int totalFileSize = 0;
			if(header.ids.size() != 0){
				totalFileSize = (header.ids.size() - 1) * BLOCK_SIZE;
				totalFileSize += header.ids.get(header.ids.size() - 1).length();
			}
			int numBlocks = (int) Math.ceil((double)(pos + size) / (double) BLOCK_SIZE);
			int firstBlockToWrite = (int) Math.ceil((double)pos / (double)BLOCK_SIZE);
			for(int i = 0; i < firstBlockToWrite - 1; i++){
				System.out.println("1");
				ContentBlock b = new ContentBlock(new byte[BLOCK_SIZE]);
				header.ids.add(_stub.put_h(serialize(b)));
			}
			if(Math.ceil((double)(pos + size) / (double) BLOCK_SIZE) == Math.ceil((double)(pos) / (double) BLOCK_SIZE)){ //se pos e pos+size no mesmo bloco
				byte[] lastBlock = new byte[(pos+size)%BLOCK_SIZE];
				System.arraycopy(contents, 0, lastBlock, pos, contents.length);
				ContentBlock block = new ContentBlock(lastBlock);
				header.ids.add(_stub.put_h(serialize(block)));
			}
			else{ //se blocos diferentes
				byte[] firstBlockWritten = new byte[BLOCK_SIZE];
				int nrBytes = size;
				int lastByteWritten = 0;
				int partOfContents = (BLOCK_SIZE -1) - pos;
				nrBytes -= partOfContents;
				lastByteWritten += partOfContents;
				System.arraycopy(contents,0,firstBlockWritten,pos,partOfContents );
				ContentBlock block  = new ContentBlock(firstBlockWritten);
				header.ids.addElement(_stub.put_h(serialize(block)));
				while(nrBytes > BLOCK_SIZE){
					byte[] newBlock = new byte[BLOCK_SIZE];
					System.arraycopy(contents,lastByteWritten,newBlock,0,BLOCK_SIZE);
					block = new ContentBlock(newBlock);
					header.ids.addElement(_stub.put_h(serialize(block)));
					nrBytes -= BLOCK_SIZE;
					lastByteWritten += BLOCK_SIZE -1;
				}
				if(nrBytes > 0){ //ainda ha mais para escrever
					byte[] lastBlockToWrite = new byte[nrBytes];
					System.arraycopy(contents, lastByteWritten, lastBlockToWrite, 0, nrBytes);
					block = new ContentBlock(lastBlockToWrite);
					header.ids.addElement(_stub.put_h(serialize(block)));
				}
			}
			byte[] b = new byte[BLOCK_SIZE];
			System.arraycopy(contents, 0, b, pos%BLOCK_SIZE, size);
			ContentBlock block = new ContentBlock(b);
			header.ids.add(_stub.put_h(serialize(block)));

			Pair<byte[], byte[]> pair = SignAndSerialize(header);
			id =_stub.put_k(pair.getValue(), pair.getKey(), keyPair.getPublic());

		    System.out.println(id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public static int FS_read(String id, int pos, int size, byte[] contents ){
		try {
			Header header = (Header) deserialize(_stub.get(id));
			for(String id2 : header.ids){
				int i = 1;
				ContentBlock block = (ContentBlock) deserialize(_stub.get(id2));
				System.out.println("block " + i++ + ":\n" + new String(block.content));
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
	
	

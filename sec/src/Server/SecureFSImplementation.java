package Server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
	
public class SecureFSImplementation extends UnicastRemoteObject implements SecureFSInterface {
	
	
	static Map<byte[],Block> blocks= new HashMap<byte[],Block>();
	
	
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
	public Vector<byte[]> get(byte[] id) throws RemoteException {
		return blocks.get(id).pieces;
	}

	@Override
	public byte[] put_k(byte[] data, Signature sig, PublicKey pubKey) throws RemoteException {
		byte[] id ;
		try {
			id = MessageDigest.getInstance("SHA256").digest(pubKey.toString().getBytes());
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			throw new RemoteException("Internal Error");
		}
		
		try{
				if(data != null){
					int remainingBytes = data.length; //regista quanto falta escrever
					int lastByteWritten = 0;
					byte[] lastBlock = blocks.get(id).pieces.lastElement();
					int freeSpace = 2048 - lastBlock.length;
					if(freeSpace > 0){ //there is some space left
				 
						int toCopy = 0;
						if(remainingBytes <= freeSpace){ //se o espaço que resta no ultimo bloco for maior que o preciso
							toCopy = remainingBytes;
						}else{
							toCopy = freeSpace;
						}
						System.arraycopy(data,0,lastBlock,freeSpace - 1, toCopy);
						remainingBytes -= toCopy;
						lastByteWritten += toCopy -1;
					}
					while(remainingBytes > 0 ){ //existe algo para escrever
						int nrBlocksRequired = remainingBytes / 2048;
						byte[] auxToWrite;
						while(nrBlocksRequired > 0){ //existem blocos inteiros por escrever
							auxToWrite = new byte[2048];
							System.arraycopy(data,lastByteWritten, auxToWrite, 0, 2048);
							blocks.get(id).pieces.addElement(auxToWrite);
							nrBlocksRequired --;
							lastByteWritten +=2048;
							remainingBytes -= 2048;	
						}
						if(remainingBytes > 0){ //existe algo menor que 2048 para escrever;
							byte[] lastBytes = new byte[remainingBytes];
							System.arraycopy(data, 0, lastByteWritten, 0, remainingBytes); 
						}	
					}				
				}

			}catch(Exception e){
				
			}
		return id;
		
	}

	@Override
	public byte[] put_h(byte[] data) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}



}
package Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Vector;

import Server.ContentBlock;
import Server.Header;
import Server.SecureFSInterface;

public class BlockManager {
	
	static final int BLOCK_SIZE = 4096;
	
	static ContentBlock emptyBlock = new ContentBlock(BLOCK_SIZE);
	static String hashEmpty;
	
	public void hashEmptyBlock(SecureFSInterface stub){
		try {
			hashEmpty = stub.put_h(serialize(emptyBlock));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getBlockByPos(int pos){
		return (int) Math.ceil((double)(pos) / (double) BLOCK_SIZE);
	}
	
	public static int getFileSize(Header head,ContentBlock lastBlock){
		int fileSize = 0;
		if(!head.ids.isEmpty()){
			fileSize +=( head.ids.size() -1) * BLOCK_SIZE;
			fileSize += lastBlock.content.length;
			
		}
		return fileSize;
	}
	//RETURN TYPE????
	public static void writeBlocks(int pos, int size, byte[] content, ContentBlock curLastBlock, Header curHeader,SecureFSInterface stub) throws Exception{
		
		int firstBlockToWrite = getBlockByPos(pos);
		int lastBlockToWrite = getBlockByPos(pos+size);
		int posInBlockCoords = pos % BLOCK_SIZE;
		int lastPosInBlockCoords = (pos+size) % BLOCK_SIZE;
		
		Vector<ContentBlock> newContentBlocksToAdd = new Vector<ContentBlock>();
		
		if(curLastBlock == null){ //there is no content in file
			curLastBlock = new ContentBlock();
		}
		
		//PADDING OPERATIONS
		//Check need for more blocks
		addPadding(stub, curHeader, firstBlockToWrite, curLastBlock, lastPosInBlockCoords); //changes header and curLastBlock
		//Writing operations
		int totalFileSize = getFileSize(curHeader, curLastBlock);
		if(totalFileSize - 1 >= pos){ //se o pos se encontra já alocado
			Vector<Integer> changedBlocks = new Vector<Integer>(); //vector to track which blocks are changed -> cases where writes are made within the blocks
			for(int aux= firstBlockToWrite; aux <= lastBlockToWrite; aux++){
				changedBlocks.add(aux);
			}
			ContentBlock firstBlock = null;
			try {
				firstBlock = (ContentBlock)  deserialize(stub.get(Integer.toString(firstBlockToWrite)));
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(firstBlock == null){throw new Exception("Error on first block to write math");}
			int numberOfFreeBytes = BLOCK_SIZE - firstBlock.content.length;
			
			Vector<ContentBlock> newContentBlocks = splitIntoBlocks(content,firstBlock);
			
			for(ContentBlock newBlock : newContentBlocks){
				String hash = stub.put_h(serialize(newBlock));
				curHeader.ids.add(firstBlockToWrite, hash);
				firstBlockToWrite++;
			}
		}
		
			/*
			if(size <= numberOfFreeBytes){ //caso simples
				System.arraycopy(content, 0, firstBlock.content,firstBlock.content.length -1, size);
				content = Arrays.copyOfRange(content, 0, size); //erase written bytes
				////////////////////// RETURN ???? ///////////////////////////////
			}else{ //size is bigger than one block
				System.arraycopy(content, 0, firstBlock.content, firstBlock.content.length -1, numberOfFreeBytes);
				content = Arrays.copyOfRange(content, numberOfFreeBytes -1,content.length );
				//split the rest into blocks
				Vector<ContentBlock> newContentBlocks = splitIntoBlocks(content,firstBlock); 
				for(int aux = 0; aux < newContentBlocks.size() ; aux++){ //get only the full blocks.
					String blockHash = stub.put_h(serialize(newContentBlocks.get(aux)));
					curHeader.ids.set(changedBlocks.get(aux), blockHash); //em principio newContentBlocks.size == changedBlocks
				}
				//last incomplete Block
				ContentBlock lastBlock = newContentBlocks.lastElement();
				ContentBlock savedBlock = (ContentBlock) deserialize(stub.get(curHeader.ids.get(changedBlocks.lastElement())));
				System.arraycopy(lastBlock.content, 0, savedBlock.content, 0, lastBlock.content.length);
				String newHash = stub.put_h(serialize(savedBlock));
				curHeader.ids.set(changedBlocks.lastElement(), newHash);
			}
			
		}else{
			//Append para o fim
			int numberOfFreeBytes = BLOCK_SIZE - curLastBlock.content.length;
			if(size <= numberOfFreeBytes){ //caso simples
				System.arraycopy(content, 0, curLastBlock.content, posInBlockCoords, content.length);
				
			}else{ 
				int posInContent = 0;
				int numberOfBytesLeft = content.length;
				System.arraycopy(content, 0, curLastBlock.content, posInBlockCoords, numberOfFreeBytes);
				posInContent += numberOfFreeBytes -1;
				numberOfBytesLeft -= numberOfBytesLeft;
				if(numberOfBytesLeft > 0){				
					int numberOfFullBlocks =  numberOfBytesLeft / BLOCK_SIZE;
					while(numberOfBytesLeft > 0){ //add all full content blocks that exist
						ContentBlock newContent = new ContentBlock();
						System.arraycopy(content, posInContent, newContent, 0, BLOCK_SIZE);
						newContentBlocksToAdd.addElement(newContent);
						numberOfBytesLeft -= BLOCK_SIZE;
						posInContent += (BLOCK_SIZE -1);
					}
					if(numberOfBytesLeft > 0){ //ainda ha algum content
						byte[] lastContent = new byte[numberOfBytesLeft];
						System.arraycopy(content, posInContent, lastContent, 0, numberOfBytesLeft);
						ContentBlock lastBlock = new ContentBlock(lastContent);
						newContentBlocksToAdd.add(lastBlock);
					}
				}
	
						
			}
			*/
		}
		
	
	private static void addPadding(SecureFSInterface stub, Header curHeader,int firstBlockToWrite,ContentBlock curLastBlock, int posInBlockCoords) throws Exception{
		int existingBlocks = curHeader.ids.size() - 1; //in array coords
		if(existingBlocks < firstBlockToWrite){ //são necessários mais blocos
			curLastBlock.content = fillBlockWithZeros(curLastBlock.content); //// fills last block with zeros
			String curLastBlockNewHash = stub.put_h(serialize(curLastBlock)); ////
			curHeader.ids.add(curHeader.ids.size() - 1, curLastBlockNewHash); ////
			int offsetBlocks = firstBlockToWrite - existingBlocks;
			
			for(;offsetBlocks >=0;offsetBlocks--){
//				newContentBlocksToAdd.add(emptyBlock);
				curHeader.ids.add(hashEmpty); //add empty block 
			}
			if(posInBlockCoords > 0){ //offset in lastBlock
				String hash = null;
				try {
					hash = stub.put_h(serialize(new ContentBlock(posInBlockCoords + 1)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(hash == null){
					throw new Exception("Error on adding new Block");
				}
				curHeader.ids.add(hash);			
				//newContentBlocksToAdd.add(new ContentBlock(posInBlockCoords));
			}
		}else { //vamos escrever num bloco que já existe
			if(posInBlockCoords > curLastBlock.content.length && firstBlockToWrite == curHeader.ids.size() - 1){ //checks if there is need for offset in the lastBlock
				int posOffset = (posInBlockCoords +1) - curLastBlock.content.length;
				byte[] offsetToAdd = new byte[posOffset];
				byte[] newLastContent = new byte[posOffset + curLastBlock.content.length];
				System.arraycopy(curLastBlock.content, 0, newLastContent, 0, curLastBlock.content.length);
				System.arraycopy(offsetToAdd, 0, newLastContent, curLastBlock.content.length , offsetToAdd.length);
				curLastBlock.content = newLastContent;
				String hash = stub.put_h(curLastBlock.content);
				curHeader.ids.add(curHeader.ids.size() - 1, hash);
			}
			
		}

	}
	
	private static Vector<ContentBlock> splitIntoBlocks(byte[] content,ContentBlock firstBlock){
		Vector<ContentBlock> newContentBlocks = new Vector<ContentBlock>();
		
		if(content.length + firstBlock.content.length <= BLOCK_SIZE){ //caso simples 
			byte[] newContent = new byte[content.length + firstBlock.content.length];
			System.arraycopy(firstBlock.content,0, newContent,0, firstBlock.content.length);
			System.arraycopy(content, 0, newContent, firstBlock.content.length, content.length);
			firstBlock.content = newContent;
			newContentBlocks.add(firstBlock);
			return newContentBlocks;
		}else {
			byte[] newContent = new byte[BLOCK_SIZE];
			int lengthFirstContentPart = BLOCK_SIZE - firstBlock.content.length;
			
			System.arraycopy(firstBlock.content,0, newContent,0, firstBlock.content.length);
			System.arraycopy(content, 0, newContent, firstBlock.content.length, lengthFirstContentPart);
			firstBlock.content = newContent;
			newContentBlocks.add(firstBlock);
			
			int remainingBytes = content.length - lengthFirstContentPart;
			content = Arrays.copyOfRange(content, lengthFirstContentPart -1, content.length -1);
			
			while(remainingBytes >= BLOCK_SIZE){
				byte[] toPutNow = Arrays.copyOfRange(content, 0, BLOCK_SIZE-1);
				content = Arrays.copyOfRange(content, BLOCK_SIZE-1, content.length - 1 );
				newContentBlocks.add(new ContentBlock(toPutNow));
			}
			//add last block
			if(content.length != 0){
				newContentBlocks.add(new ContentBlock(content));
			}
			return newContentBlocks;
			
		}
		
		
	}
	
	public static byte[] fillBlockWithZeros(byte[] toFill){
		int numberOfZeros = BLOCK_SIZE - toFill.length;
		byte[] toReturn =  new byte[toFill.length + numberOfZeros];
		byte[] zerosArr = new byte[numberOfZeros];
		System.arraycopy(toFill, 0, toReturn, 0, toFill.length);
		System.arraycopy(zerosArr, 0, toReturn, toFill.length - 1, numberOfZeros);
		return toReturn;
	}
	
	public static int getPosOfLastByteInBlock(ContentBlock contBlock){
		return ((BLOCK_SIZE - contBlock.content.length) -2); 
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

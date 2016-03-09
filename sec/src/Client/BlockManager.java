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
import javafx.util.Pair;

public class BlockManager {
	
	static final int BLOCK_SIZE = 4096;
	
	static byte[] emptyBlock = new byte[BLOCK_SIZE];
	static String hashEmpty;
	
	/*
	public void hashEmptyBlock(SecureFSInterface stub){
		try {
			hashEmpty = stub.put_h(serialize(emptyBlock));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	public static int getBlockByPos(int pos){
		return (int) Math.floor((double)(pos) / (double) BLOCK_SIZE);
	}
	
	public static int getFileSize(Header head, byte[] contentLastBlock){
		int fileSize = ( head.ids.size() -1) * BLOCK_SIZE;
			fileSize += contentLastBlock.length;
		return fileSize;
	}
	
	public static int[] getBlockIndices(int pos, int size){
		int firstBlockToWrite = getBlockByPos(pos);
		int lastBlockToWrite = getBlockByPos(pos+size);
		int totalBlocks = lastBlockToWrite - firstBlockToWrite + 1;
		int[] result = new int[totalBlocks];
		for(int i = 0; i < totalBlocks; i++){
			result[i] = firstBlockToWrite + i;
		}
		return result;
	}
	
	public static int[] getBlockIndicesToPad(int pos, int totalSize){
		int lastWrittenBlock = getBlockByPos(totalSize);
		int lastBlockToPad = getBlockByPos(pos);
		int numBlocks = 0;
		int relativePos = pos % BLOCK_SIZE;
		int relativeEnd = totalSize % BLOCK_SIZE;
		
		if(lastBlockToPad > lastWrittenBlock){
			numBlocks += lastBlockToPad - lastWrittenBlock;	
			//somar 1 se o eof nao estiver no fim do bloco
			if(relativeEnd != 0) numBlocks++;
			if(totalSize == 0) numBlocks++;
		}
		
		if(lastBlockToPad == lastWrittenBlock && relativePos > relativeEnd){
			numBlocks++;
		}
				
		if(numBlocks == 0){
			int[] result = new int[1];
			result[0] = -1;
			return result;
		}
		int[] result = new int[numBlocks];
		for(int i = numBlocks, j = 0; i > 0; i--, j++){
			result[i - 1] = lastBlockToPad - j;	
		}
		return result;
	}
	
	/*
	//RETURN TYPE????
	public static void writeBlocks(int totalFileSize, int pos, int size, byte[] content, ContentBlock curLastBlock, Header curHeader,SecureFSInterface stub) throws Exception{

		int firstBlockToWrite = getBlockByPos(pos);
		int lastBlockToWrite = getBlockByPos(pos+size);
		int posInBlockCoords = pos % BLOCK_SIZE;
		int lastPosInBlockCoords = (pos+size) % BLOCK_SIZE;

		Vector<ContentBlock> newContentBlocksToAdd = new Vector<ContentBlock>();

		if(curLastBlock == null){ //there is no content in file
			curLastBlock = new ContentBlock();
		}

		//Writing operations
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

	}
*/

	
	public static Vector<byte[]> addPadding(int existingBlocks,int lastBlockToWrite,byte[] curLastBlockContent, int posInBlockCoords){
		Vector<byte[]> result = new Vector<byte[]>();
		if(curLastBlockContent == null) curLastBlockContent = new byte[0];
		if(existingBlocks < lastBlockToWrite){ //são necessários mais blocos
			
			System.out.println("need more blocks");
			
			result.add(fillBlockWithZeros(curLastBlockContent)); //// fills last block with zeros
			
			if(existingBlocks == -1) existingBlocks = 0;
			
			int offsetBlocks = lastBlockToWrite - existingBlocks - 1;
			
			//creates full blocks of 0s between the first and last block to pad
			for(;offsetBlocks > 0;offsetBlocks--){
				System.out.println("add empty block");
				result.add(emptyBlock);
			}
			//fills last block with 0s until the pos
			if(posInBlockCoords > 0){ //offset in lastBlock
				
				System.out.println("final block");
				String hash = null;
				result.add(new byte[posInBlockCoords]);		
			}
		}else { //vamos escrever num bloco que já existe
			if(posInBlockCoords > curLastBlockContent.length + 1 && lastBlockToWrite == existingBlocks){ //checks if there is need for offset in the lastBlock
				int numZeros = posInBlockCoords - curLastBlockContent.length;
				byte[] byteOfZeros = new byte[numZeros];
				byte[] newLastContent = new byte[posInBlockCoords];
				System.arraycopy(curLastBlockContent, 0, newLastContent, 0, curLastBlockContent.length);
				System.arraycopy(byteOfZeros, 0, newLastContent, curLastBlockContent.length , numZeros);
				result.add(newLastContent);
			}			
		}
		return result;
	}
	
	public static Vector<byte[]> newBlocks(Pair<byte[],byte[]> oldContent, int pos, byte[] content){
		int initialPos = pos % BLOCK_SIZE;
		int finalPos = (content.length + pos) % BLOCK_SIZE;		
		Vector<byte[]> result = new Vector<byte[]>();
		
		System.arraycopy(content, 0, oldContent.getKey(), pos, BLOCK_SIZE - pos);
		
		/*
		if(numBlocks == 1){
			int lengthOldContent = oldContent.elementAt(0).length;
			int newSize = Math.max(lengthOldContent, finalPos);
			byte[] newBlock = new byte[newSize];
			System.arraycopy(oldContent.elementAt(0), 0, newBlock, 0, initialPos);
			System.arraycopy(content, 0, newBlock, initialPos, size);
			if(lengthOldContent > finalPos)
				System.arraycopy(oldContent.elementAt(0), finalPos, newBlock, finalPos, newSize-finalPos);
			result.add(newBlock);
		}
		else{
			byte[] firstNewBlock = new byte[BLOCK_SIZE];
			System.arraycopy(oldContent.elementAt(0), 0, firstNewBlock, 0, initialPos);
			System.arraycopy(content, 0, firstNewBlock, initialPos, size);
			
		}
		*/
		return oldContent;
	}
	
	public static Vector<byte[]> splitIntoBlocks(byte[] content, byte[] firstBlock){
		Vector<byte[]> newContentBlocks = new Vector<byte[]>();
		
		if(firstBlock == null) firstBlock = new byte[0];
		
		if(content.length + firstBlock.length <= BLOCK_SIZE){ //caso simples 
			byte[] newContent = new byte[content.length + firstBlock.length];
			System.arraycopy(firstBlock,0, newContent,0, firstBlock.length);
			System.arraycopy(content, 0, newContent, firstBlock.length, content.length);
			newContentBlocks.add(newContent);
			return newContentBlocks;
		}
		return newContentBlocks;
		/*else {
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
		
		*/
	}
	
	public static byte[] fillBlockWithZeros(byte[] toFill){
		int numberOfZeros = BLOCK_SIZE - toFill.length;
		byte[] toReturn =  new byte[BLOCK_SIZE];
		byte[] zerosArr = new byte[numberOfZeros];
		System.arraycopy(toFill, 0, toReturn, 0, toFill.length);
		System.arraycopy(zerosArr, 0, toReturn, toFill.length, numberOfZeros);
		return toReturn;
	}
	/*
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
	}*/
	
	
	
}

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
	
	public static final int BLOCK_SIZE = 3;
	
	static byte[] emptyBlock = new byte[BLOCK_SIZE];
	public static String hashEmpty;
	
	
	public static void hashEmptyBlock(SecureFSInterface stub){
		try {
			hashEmpty = stub.put_h(emptyBlock);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
		int lastBlockToWrite = getBlockByPos(pos+size-1);
		int totalBlocks = lastBlockToWrite - firstBlockToWrite + 1;
		int[] result = new int[totalBlocks];
		for(int i = 0; i < totalBlocks; i++){
			result[i] = firstBlockToWrite + i;
		}
		return result;
	}
	
	
	public static int[] getBlockIndicesToPad(int pos, int totalSize){
		
		
		//file was not yet created
		if(totalSize == 0){
			int lastBlockToPad = getBlockByPos(pos);
			int numBlocks = lastBlockToPad + 1;
			int[] result = new int[numBlocks];
			for(int i = 0; i < numBlocks; i++){
				result[i] = i;	
			}
			return result;
		} //TODO create the else (code below but with less checks)
		
		int lastWrittenBlock = getBlockByPos(totalSize - 1);
		int lastBlockToPad = getBlockByPos(pos);
		int numBlocks = 0;
		int relativePos = pos % BLOCK_SIZE;
		int relativeEnd = (totalSize - 1 )% BLOCK_SIZE;
		if(relativeEnd == -1) relativeEnd = 2;
		
		if(lastWrittenBlock == -1) lastWrittenBlock = 0;
		
		if(lastBlockToPad > lastWrittenBlock){
			numBlocks += lastBlockToPad - lastWrittenBlock;	
			//somar 1 se o eof nao estiver no fim do bloco
			if(relativeEnd != 2) numBlocks++;
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

	
	public static Vector<byte[]> addPadding(int existingBlocks,int lastBlockToWrite,byte[] curLastBlockContent, int posInBlockCoords){
		Vector<byte[]> result = new Vector<byte[]>();
		if(curLastBlockContent == null) curLastBlockContent = new byte[0];
		if(existingBlocks == -1) existingBlocks = 0;
		if(existingBlocks < lastBlockToWrite){ //são necessários mais blocos
			
			
			
			result.add(fillBlockWithZeros(curLastBlockContent)); //// fills last block with zeros
			
			int offsetBlocks = lastBlockToWrite - existingBlocks - 1;
			
			//creates full blocks of 0s between the first and last block to pad
			for(;offsetBlocks > 0;offsetBlocks--){
				
				result.add(emptyBlock);
			}
			//fills last block with 0s until the pos	
				
				String hash = null;
				result.add(new byte[posInBlockCoords+1]);		
			
		}else { //vamos escrever num bloco que já existe
				int numZeros = posInBlockCoords - curLastBlockContent.length;
				byte[] byteOfZeros = new byte[numZeros];
				byte[] newLastContent = new byte[posInBlockCoords+1];
				System.arraycopy(curLastBlockContent, 0, newLastContent, 0, curLastBlockContent.length);
				System.arraycopy(byteOfZeros, 0, newLastContent, curLastBlockContent.length , numZeros);
				result.add(newLastContent);
		}
		return result;
	}
	
	public static Vector<byte[]> newBlocks(Pair<byte[],byte[]> oldContent, int pos, byte[] content, int nrBlocksModified){
		int initialPos = pos % BLOCK_SIZE;
		int finalPos = (pos+content.length) % BLOCK_SIZE; 	
		Vector<byte[]> result = new Vector<byte[]>();

		if(nrBlocksModified == 1){
			System.arraycopy(content, 0, oldContent.getKey(), initialPos, content.length);
			result.add(oldContent.getKey());
		}else{
			//Add content in first block from pos
			System.arraycopy(content, 0, oldContent.getKey(), initialPos, BLOCK_SIZE - initialPos);
			content = Arrays.copyOfRange(content, BLOCK_SIZE - initialPos,content.length ); //erase content already written
			result.add(oldContent.getKey());
			
			//copy full blocks
			while(content.length > BLOCK_SIZE){
				byte[] newFullBlock = new byte[BLOCK_SIZE];
				System.arraycopy(content, 0, newFullBlock, 0, BLOCK_SIZE);
				content = Arrays.copyOfRange(content,  BLOCK_SIZE, content.length);
				result.add(newFullBlock);
			}
			
			if(content.length > 0){ //if there is smth left
				System.arraycopy(content, 0, oldContent.getValue(), 0, content.length);
				result.add(oldContent.getValue());
			}	
		}
		
		return result;
	}
	
	public static byte[] fillBlockWithZeros(byte[] toFill){
		int numberOfZeros = BLOCK_SIZE - toFill.length;
		byte[] toReturn =  new byte[BLOCK_SIZE];
		byte[] zerosArr = new byte[numberOfZeros];
		System.arraycopy(toFill, 0, toReturn, 0, toFill.length);
		System.arraycopy(zerosArr, 0, toReturn, toFill.length, numberOfZeros);
		return toReturn;
	}
	
}

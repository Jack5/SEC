package firstPart;

import Client.BlockManager;
import Client.Buffer;
import Client.FSLib;
import Exceptions.InvalidContentException;
import Exceptions.InvalidSignatureException;
import Exceptions.WrongStorageException;
import junit.framework.TestCase;

import java.security.InvalidKeyException;

import org.junit.*;

public class testsFSWRITE extends TestCase {

	String id;

	public String createStringSizeX(int desiredSize, String repeatedPattern){
		String toReturn = new String();
		for(; desiredSize > 0 ; desiredSize--){
			toReturn += repeatedPattern;
		}
		return toReturn;
	}
	
	
	@Before //before every test or not
	public void initialize(){
		FSLib.FS_init();
		id = FSLib.getId();
		
		
	}
	
	public void initializeBlocksAlreadyExist(){
		FSLib.FS_init();
		id = FSLib.getId();
		
		Buffer buf = new Buffer();
		String BIGCONTENT = createStringSizeX(BlockManager.BLOCK_SIZE, "a");
		buf.setContent(BIGCONTENT.getBytes());
		try {
			FSLib.FS_write(0,buf.getContent().length, buf);
		} catch ( InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	@Test
	public void testfirstPosOneBlockOnEmptyFile(){
		initialize();
		Buffer buf = new Buffer();
		buf.setContent("OneBlock".getBytes());
		
		try {
			FSLib.FS_write(0,buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] expected = "OneBlock".getBytes();
		int bytesRead = 0;
		try {
			bytesRead = FSLib.FS_read(id, 0, 9000000, buf);
		} catch (InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //read all
			catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		assertEquals(expected.length ,bytesRead);
		assertEquals(new String(expected),new String(buf.getContent()));
		
	}
	
	@Test
	public void testfirstPosMoreBlocksOnEmptyFile(){
		initialize();
		Buffer buf = new Buffer();
		buf.setContent(new byte[BlockManager.BLOCK_SIZE + 1]);
		
		try {
			FSLib.FS_write(0,buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] expected = new byte[BlockManager.BLOCK_SIZE + 1];
		
		int bytesRead = 0;
		try {
			bytesRead = FSLib.FS_read(id, 0, 9000000, buf);
		} catch (InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //read all
		catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length ,bytesRead);
		assertEquals(new String(expected),new String(buf.getContent()));
	}
	
	@Test
	public void testOtherPosSameBlockOnEmptyFile(){
		initialize();
		Buffer buf = new Buffer();
		buf.setContent("OneBlock".getBytes());
		
		try {
			FSLib.FS_write(3, buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] expected = new byte["OneBlock".getBytes().length + "000".getBytes().length];
		System.arraycopy("OneBlock".getBytes(), 0, expected, 3, "OneBlock".getBytes().length);

		int bytesRead = 0;
		try {
			bytesRead = FSLib.FS_read(id, 0, 9000000, buf);
		} catch ( InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //read all
		catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length, bytesRead);
		assertEquals(new String(expected), new String(buf.getContent()));
		
	}
	
	@Test
	public void testOtherPosMoreBlocksOnEmptyFile(){
		initialize();
		Buffer buf = new Buffer();
		buf.setContent(new byte[BlockManager.BLOCK_SIZE + 1]);
		
		try {
			FSLib.FS_write(3, buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] expected = new byte[BlockManager.BLOCK_SIZE + 4];
		

		int bytesRead = 0;
		try {
			bytesRead = FSLib.FS_read(id, 0, 9000000, buf);
		} catch ( InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //read all
		catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length, bytesRead);
		assertEquals(new String(expected), new String(buf.getContent()));
		
	}
	
	@Test
	public void testOtherPosStartingAndEndingOnAnotherBlockOnEmptyFile(){
		initialize();
		Buffer buf = new Buffer();
		buf.setContent("On".getBytes());
		
		try {
			FSLib.FS_write(BlockManager.BLOCK_SIZE - 1, buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] expected = new byte[2 + "On".getBytes().length];
		System.arraycopy("On".getBytes(), 0, expected, 2, 2);
		

		int bytesRead = 0;
		try {
			bytesRead = FSLib.FS_read(id, 0, 9000000, buf);
		} catch ( InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //read all
 catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
		
		assertEquals(expected.length, bytesRead);
		assertEquals(new String(expected), new String(buf.getContent()));
		
	}
	
	@Test
	public void testOtherPosStartingAndEndingInDifferentBlocksOnEmptyFile(){
		initialize();
		Buffer buf = new Buffer();
		buf.setContent(new byte[BlockManager.BLOCK_SIZE + 1]);
		
		try {
			FSLib.FS_write(1, buf.getContent().length, buf);
		} catch ( InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] expected = new byte[BlockManager.BLOCK_SIZE + 2];
		

		int bytesRead = 0;
		try {
			bytesRead = FSLib.FS_read(id, 0, 9000000, buf);
		} catch ( InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //read all
 catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
		
		assertEquals(expected.length, bytesRead);
		assertEquals(new String(expected), new String(buf.getContent()));
		
	}
	
	
	@Test
	public void testOverwriteSameBlockWithoutPadding(){
		initializeBlocksAlreadyExist();
		
		Buffer buf = new Buffer();
		buf.setContent("Te".getBytes());
		
		try {
			FSLib.FS_write(0, buf.getContent().length , buf);
		} catch ( InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String oldContent = createStringSizeX(BlockManager.BLOCK_SIZE, "a");
		
		byte[] expected =( "Te" + oldContent.substring(buf.getContent().length , oldContent.length())).getBytes();
		
		
		int bytesRead = 0;
		try {
			bytesRead = FSLib.FS_read(id, 0, 9000000, buf);
		} catch ( InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length, bytesRead);
		assertEquals(new String(expected), new String(buf.getContent()));
	}
	
	@Test
	public void testOverwriteOnMultipleBlocks(){
		initialize();
		
		Buffer buf = new Buffer();
		
		String fillerContent = createStringSizeX(BlockManager.BLOCK_SIZE * 2 , "a");
		buf.setContent(fillerContent.getBytes());
		try {
			FSLib.FS_write(0, fillerContent.length(), buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String toReplace = createStringSizeX(BlockManager.BLOCK_SIZE, "b");
		buf.setContent(toReplace.getBytes());
		try {
			FSLib.FS_write(1, buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String expected = "a" + createStringSizeX(BlockManager.BLOCK_SIZE, "b") + createStringSizeX(BlockManager.BLOCK_SIZE - 1 , "a");
		
		
		int nrBytesRead = 0;
		try {
			nrBytesRead = FSLib.FS_read(id, 0, 9000000, buf);
		} catch (InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length(), nrBytesRead);
		assertEquals(expected, new String(buf.getContent()) );
	}
	
	@Test
	public void testWriteAfterEOFSameBlock(){
		initialize();
		
		Buffer buf = new Buffer();
		
		String fillerContent = "a";
		buf.setContent(fillerContent.getBytes());
		try {
			FSLib.FS_write(0, fillerContent.length(), buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String toReplace = "b";
		buf.setContent(toReplace.getBytes());
		try {
			FSLib.FS_write(2, buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String expected = "a" + ((char) 0) + "b" ;
		
		int nrBytesRead = 0;
		try {
			nrBytesRead = FSLib.FS_read(id, 0, 90000000, buf);
		} catch (InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length(), nrBytesRead);
		assertEquals(expected, new String(buf.getContent()));
		
	}
	
	@Test
	public void testWriteAfterEOFPosteriorBlock(){
		initialize();
		Buffer buf = new Buffer();
		
		String fillerContent = "a";
		buf.setContent(fillerContent.getBytes());
		try {
			FSLib.FS_write(0, fillerContent.length(), buf);
		} catch ( InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String toReplace = "b";
		buf.setContent(toReplace.getBytes());
		try {
			FSLib.FS_write(4, buf.getContent().length, buf);
		} catch ( InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String expected = "a" + createStringSizeX(BlockManager.BLOCK_SIZE, new String(new byte[1]))+  "b" ;
		
		int nrBytesRead = 0;
		try {
			nrBytesRead = FSLib.FS_read(id, 0, 90000000, buf);
		} catch (InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length(), nrBytesRead);
		assertEquals(expected, new String(buf.getContent()));
	}
	
	@Test
	public void testWriteBeforeEOFFinishAfterEOFSameBlock(){
		
		initialize();
		Buffer buf = new Buffer();
		
		String fillerContent = createStringSizeX(BlockManager.BLOCK_SIZE + 1 , "a");
		buf.setContent(fillerContent.getBytes());
		try {
			FSLib.FS_write(0, fillerContent.length(), buf);
		} catch ( InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String toReplace = createStringSizeX(BlockManager.BLOCK_SIZE , "b");
		buf.setContent(toReplace.getBytes());
		try {
			FSLib.FS_write(2, buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String expected = createStringSizeX(2, "a") +  createStringSizeX(3, "b");
		
		int nrBytesRead = 0;
		try {
			nrBytesRead = FSLib.FS_read(id, 0, 90000000, buf);
		} catch ( InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length(), nrBytesRead);
		assertEquals(expected, new String(buf.getContent()));
	}
	
	@Test
	public void testWriteBeforeEOFFinishAfterEOFPosteriorBlock(){
		
		initialize();
		Buffer buf = new Buffer();
		
		String fillerContent = createStringSizeX(BlockManager.BLOCK_SIZE + 1 , "a");
		buf.setContent(fillerContent.getBytes());
		try {
			FSLib.FS_write(0, fillerContent.length(), buf);
		} catch ( InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String toReplace = createStringSizeX(5 , "b");
		buf.setContent(toReplace.getBytes());
		try {
			FSLib.FS_write(2, buf.getContent().length, buf);
		} catch (InvalidSignatureException | InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String expected = createStringSizeX(2, "a") +  createStringSizeX(5, "b");
		
		int nrBytesRead = 0;
		try {
			nrBytesRead = FSLib.FS_read(id, 0, 90000000, buf);
		} catch ( InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(expected.length(), nrBytesRead);
		assertEquals(expected, new String(buf.getContent()));
	}
	
}

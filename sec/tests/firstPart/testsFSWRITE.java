package firstPart;

import Client.BlockManager;
import Client.Buffer;
import Client.FSLib;
import junit.framework.TestCase;
import org.junit.*;

public class testsFSWRITE extends TestCase {

	String id;

	
	@Before //before every test or not
	public void initialize(){
		FSLib.FS_init();
		id = FSLib.getId();
		

	}
	
	
	@Test
	public void testSameBlockPadding(){
			
		initialize();
		Buffer buf = new Buffer();
		buf.setContent("testPadding".getBytes());
		
		FSLib.FS_write(20,buf.getContent().length, buf);
		
		byte[] expected = new byte["00000000000000000000".length() + buf.getContent().length];
		System.arraycopy("testPadding".getBytes(), 0, expected, "0000000".length() - 1, buf.getContent().length);
		

		FSLib.FS_read(id, 0, 9000000, buf);
		
		assertEquals(expected,buf.getContent());
	}
	
	@Test
	public void testWithoutPadding(){
		initialize();
		Buffer buf = new Buffer();
		buf.setContent("testNoPadding".getBytes());
		
		FSLib.FS_write(0, "testNoPadding".length(), buf);
		

		
		byte[] expected = "testNoPadding".getBytes();
		
		FSLib.FS_read(id, 0, 90000000, buf);
		
		assertEquals(new String (expected), new String (buf.getContent()));
		
	}
	
	@Test
	public void testMultipleBlocksPadding(){
		initialize();
		Buffer buf = new Buffer();
		buf.setContent("farAway".getBytes());
		
		FSLib.FS_write(0,8192 , buf);
		
		byte[] expected = new byte[BlockManager.BLOCK_SIZE * 2 + "farAway".length() ];
		byte[] empty = new byte[BlockManager.BLOCK_SIZE];
		System.arraycopy(empty, 0, expected, 0, BlockManager.BLOCK_SIZE);
		System.arraycopy(empty, 0, expected,  BlockManager.BLOCK_SIZE - 1, BlockManager.BLOCK_SIZE);
		System.arraycopy("farAway".getBytes(),0, expected, (BlockManager.BLOCK_SIZE * 2) - 1, "farAway".length());
		
		
		FSLib.FS_read(id, 0, 900000, buf);
		
		assertEquals(expected,buf.getContent());
		
	}
	
	
	
}

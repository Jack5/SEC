package secondPart;

import Client.Buffer;
import Client.FSLib;
import Exceptions.InvalidContentException;
import Exceptions.InvalidSignatureException;
import Exceptions.WrongStorageException;
import junit.framework.TestCase;

import static org.junit.Assert.*;

import java.security.InvalidKeyException;

import org.junit.*;


public class testsFSREAD extends TestCase {

	//tests that read using publicKey as identifier works properly
	@Test
	public void testCorrectRead(){
		FSLib.FS_init();	
		int numberOfBytesRead = 0;
		Buffer b = new Buffer();
		b.setContent("12345".getBytes());
		try {
			FSLib.FS_write(0, 5, b);
			numberOfBytesRead = FSLib.FS_read(FSLib.cert.getPublicKey(), 0, 10, b);
		} catch (Exception e) {
			fail();
		}

		assertEquals(5, numberOfBytesRead);
		assertEquals("12345", new String(b.getContent()));		
	}
	
}

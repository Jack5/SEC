package secondPart;

import junit.framework.TestCase;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Vector;

import org.junit.*;
import java.util.Random;
import Client.Buffer;
import Client.FSLib;
import Exceptions.InvalidContentException;
import Exceptions.InvalidSignatureException;
import Exceptions.WrongStorageException;
public class testIntegrity extends TestCase{
	
	//init fslib with one CC but try writting with another
	@Test
	public void testSwitchingCC() {

		FSLib.FS_init();
		
		Buffer b = new Buffer();
		b.setContent("1".getBytes());	
		
		try {
			System.in.read();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			FSLib.FS_write(0, 1, b);
		} catch (InvalidSignatureException e) {
			assertTrue(true);
		} catch (Exception e){
			fail();
		}
		fail();
	}

}

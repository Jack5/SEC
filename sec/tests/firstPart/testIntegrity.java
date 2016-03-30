package firstPart;

import junit.framework.TestCase;

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
	
	String id = null;
	
	@Before
	public void initialize() throws InvalidSignatureException, InvalidContentException, Exceptions.InvalidKeyException, WrongStorageException{
		FSLib.FS_init();
		
		id = FSLib.getId();
		
		Buffer buf = new Buffer();
		
		byte[] text = "a".getBytes();
		
		buf.setContent(text);
		FSLib.FS_write(0,text.length, buf);	
		

	}
	
	@Test(expected = InvalidSignatureException.class)
	public void testChangeCBIdInHeader() throws InvalidSignatureException, InvalidContentException, Exceptions.InvalidKeyException, WrongStorageException {
		initialize();
		boolean validTest = false;
		FSLib.changeIdVector(0, "imFake", id);
		
		Buffer bufa = new Buffer();
		
		try {
			FSLib.FS_read(id, 0, 9000000, bufa);
		} catch (InvalidSignatureException e) {
			validTest = true;
		}
		
		assertTrue(validTest);
		
	}
	
	@Test(expected = InvalidSignatureException.class)
	public void testChangeSignedHashInHeader() {
		try {
			initialize();
		} catch (InvalidSignatureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidContentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exceptions.InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WrongStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean validTest = false;
		FSLib.changeSignedHash(new byte[256],id);
		
		Buffer bufa = new Buffer();
	
		try {
			FSLib.FS_read(id, 0, 9000000, bufa);
		} catch (Exceptions.InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidSignatureException e) {
			validTest = true;
	
		} catch (InvalidContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(validTest);
	}
	
	@Test(expected = InvalidKeyException.class)
	public void testChangePubKeyInHeader() throws InvalidSignatureException, InvalidContentException, Exceptions.InvalidKeyException, WrongStorageException{
		initialize();
		boolean isValid = false;
		KeyPairGenerator fakePairGen = null;
		try {
			fakePairGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SecureRandom secRand = null;
		try {
			secRand = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fakePairGen.initialize(2048, secRand);
		KeyPair keyPair = fakePairGen.generateKeyPair();
		
		
		FSLib.changePublicKey(keyPair.getPublic(), id);
		
		Buffer bufa = new Buffer();
		
		try {
			FSLib.FS_read(id, 0, 9000000, bufa);
		} catch (Exceptions.InvalidKeyException e) {
			isValid = true;

		} catch (InvalidSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(isValid);
	}
	
	@Test(expected = InvalidContentException.class)
	public void testChangeContentInCB() throws InvalidSignatureException, Exceptions.InvalidKeyException, InvalidContentException, WrongStorageException{
		initialize();
		boolean isValid = false;
		
		Vector<String> originalIds = FSLib.getCBIdsFromHeader(id);
		Random rand = new Random();
		int randomNumber = 0;
		if(originalIds.size() != 1){
			randomNumber = rand.nextInt(originalIds.size() - 1);
		}

		
		FSLib.changeContentBlock("fakeContent".getBytes(), originalIds.elementAt(randomNumber));
		
		Buffer bufa = new Buffer();
		
		try {
			FSLib.FS_read(id, 0, 9000000, bufa);
		} catch (InvalidContentException e) {
			isValid = true;
		}
		 assertTrue(isValid);
	}
	
}

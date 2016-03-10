package firstPart;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import Client.FSLib;
import junit.framework.TestCase;
import org.junit.Test;

public class testsFSINIT extends TestCase {
	
	//testa que o id do ficheiro e o hash da chave publica
	@Test
	public void testId(){
		FSLib.FS_init();
		String hashedPubKey = null;
		try {
			hashedPubKey = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(FSLib.getPubKey().toString().getBytes()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String clientId = FSLib.getId();
		assertEquals(hashedPubKey,clientId);
	}
	
	
	
	
}

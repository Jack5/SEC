package firstPart;

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

	String id = null;
	String text ="Asarmas e os barões assinalados,Que da ocidental praia Lusitana,Por mares nunca de antes navegados,Passaram ainda além da Taprobana,Em perigos e guerras esforçados,Mais do que prometia a força humana,E entre gente remota edificaram Novo Reino, que tanto sublimaram";
	
	boolean ffs = false;
	
	@Before
	public void initialize() throws InvalidSignatureException, InvalidContentException, Exceptions.InvalidKeyException, WrongStorageException{
		FSLib.FS_init();
		
		id = FSLib.getId();
		
		Buffer buf = new Buffer();
		
		buf.setContent(text.getBytes());
		FSLib.FS_write(0, text.length(), buf);
		ffs = true;
	}
	
	@Test
	public void testCorrectRead(){
		if(!ffs){
			try {
				initialize();
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
		}
		Buffer result = new Buffer();
		int numberOfBytesRead = 0;
		try {
			numberOfBytesRead = FSLib.FS_read(id, 0, 5, result);
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
		
		String textFrag = new String(text.substring(0,5));
		assertEquals(numberOfBytesRead, 5);
		assertEquals(textFrag, new String(result.getContent()));		
	}
	
	@Test
	public void testReadBegginingBeyondLimits(){
		if(!ffs){
			try {
				initialize();
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
		}
		Buffer result = new Buffer();
		
		int numberOfBytesRead = 0;
		try {
			numberOfBytesRead = FSLib.FS_read(id, 900000000, 90000000, result);
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
		assertEquals(null,result.getContent());
		assertEquals(numberOfBytesRead,0);
		
	}
	
	@Test
	public void testReadBeyondEOF(){
		if(!ffs){
			try {
				initialize();
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
		}
		Buffer result = new Buffer();
		int numberOfBytesRead = 0;
		try {
			numberOfBytesRead = FSLib.FS_read(id, 0, 90000000, result);
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
		
		
		assertEquals(new String(result.getContent()),text);
		assertEquals(numberOfBytesRead,text.length());
		
	}
	
}

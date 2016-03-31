package secondPart;

import Client.BlockManager;
import Client.Buffer;
import Client.FSLib;
import Exceptions.InvalidContentException;
import Exceptions.InvalidSignatureException;
import Exceptions.WrongStorageException;
import Server.CCLogic;
import javafx.util.Pair;
import junit.framework.TestCase;

import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Vector;

import org.junit.*;

public class testsFSWRITE extends TestCase {

	//tests write with CC card to authenticate
		@Test
		public void testWrite(){
			FSLib.FS_init();	
			try {
				Buffer b = new Buffer();
				b.setContent("12345".getBytes());
				FSLib.FS_write(0, 5, b);
				assertTrue(true);
			} catch (Exception e) {
				fail();
			}
		}
	
}

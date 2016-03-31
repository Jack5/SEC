package secondPart;

import java.rmi.RemoteException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.junit.Test;

import Client.FSLib;
import Server.CCLogic;
import junit.framework.TestCase;

public class testsFSINIT extends TestCase {

	//tests that the certificate of the public key is stored on the server
	@Test
	public void testStoredCertificate(){
		FSLib.FS_init();
		X509Certificate certCC;
		try {
			certCC = CCLogic.getCertificate(0);		
			X509Certificate storedCert = (X509Certificate) FSLib.FS_list().get(0).getValue();
			assertEquals(certCC, storedCert);
		} catch (CertificateException | RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

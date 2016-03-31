package secondPart;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Vector;


import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;
import org.junit.Test;

import Client.FSLib;
import Server.CCLogic;
import javafx.util.Pair;
import junit.framework.TestCase;
import pteidlib.PteidException;

public class testsFSLIST extends TestCase {

	//tests that list returns both the id and the certificate of one CC
	@Test
	public void testListWithOneCard(){
		FSLib.FS_init();	
		try {
			Vector<Pair<String, Certificate>> list = FSLib.FS_list();
			assertEquals(list.size(),1);
			X509Certificate certCC = CCLogic.getCertificate(0);			
			X509Certificate storedCert = (X509Certificate) list.get(0).getValue();
			assertEquals(certCC, storedCert);
			
		} catch (CertificateException | RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			FSLib.cleanseCerts();
		}
	}

	//tests that list returns both the id and the certificate of two CCs
	@Test
	public void testListWithTwoCards(){
		FSLib.FS_init();	
		try {
			String userName = "12345678 1 zz1";

			CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
			// generate it with 2048 bits
			certGen.generate(2048);
			// prepare the validity of the certificate
			long validSecs = (long) 365 * 24 * 60 * 60; // valid for one year
			// add the certificate information, currently only valid for one year.
			X509Certificate cert = certGen.getSelfCertificate(
					// enter your details according to your application
					new X500Name("CN=My Application,O=My Organisation,L=My City,C=DE"), validSecs);

			FSLib.addDummyCert(cert, userName);
			
			Vector<Pair<String, Certificate>> list = FSLib.FS_list();
			assertEquals(list.size(),2);
			X509Certificate certCC = CCLogic.getCertificate(0);			
			X509Certificate storedCert = (X509Certificate) list.get(0).getValue();
			assertEquals(certCC, storedCert);
			assertEquals(cert, list.get(1).getValue());
		} catch (CertificateException | InvalidKeyException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			FSLib.cleanseCerts();
		}
	}
}

package secondPart;

import java.security.cert.X509Certificate;

import org.junit.Test;

import Client.FSLib;
import junit.framework.TestCase;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;
public class testCertificates extends TestCase{
	
	//tests that invalid certificates are found as so
	@Test
	public void testInvalidCertificate() {
		try{
			FSLib.loadRootCAandCRL();
			CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
			// generate it with 2048 bits
			certGen.generate(2048);
			// prepare the validity of the certificate
			long validSecs = (long) 365 * 24 * 60 * 60; // valid for one year
			// add the certificate information, currently only valid for one year.
			X509Certificate cert = certGen.getSelfCertificate(
					// enter your details according to your application
					new X500Name("CN=My Application,O=My Organisation,L=My City,C=DE"), validSecs);
			assertFalse(FSLib.checkCertificate(cert));
		} catch(Exception e){
			fail();
		}
	}
}

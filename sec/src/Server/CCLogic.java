package Server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.CK_SESSION_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;


public class CCLogic {
	
	static boolean isLogin = false;
	
	public static X509Certificate getCertificate(int n) throws CertificateException{
		return getCertFromByteArray(getCertificateInBytes(n));
	}

	public static String getCardNumber() throws PteidException{
		return pteid.GetID().cardNumber;
	}


	public static byte[] sign(byte[] data ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, PKCS11Exception, PteidException{	
		
		java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
		PKCS11 pkcs11;
		String osName = System.getProperty("os.name");
		String javaVersion = System.getProperty("java.version");
		String libName = "libbeidpkcs11.so";
		if (-1 != osName.indexOf("Windows"))
			libName = "pteidpkcs11.dll";
		else if (-1 != osName.indexOf("Mac"))
			libName = "pteidpkcs11.dylib";
		Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
		if (javaVersion.startsWith("1.5."))
		{
			Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
			pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, null, false });
		}
		else
		{
			Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
			pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
		}

		//Open the PKCS11 session
		long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

		// Token login 
		if(!isLogin){
			pkcs11.C_Login(p11_session, 1, null);
			isLogin = true;
		}

		CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);

		// Get available keys
		CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
		attributes[0] = new CK_ATTRIBUTE();
		attributes[0].type = PKCS11Constants.CKA_CLASS;
		attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

		pkcs11.C_FindObjectsInit(p11_session, attributes);
		long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);

		long signatureKey = keyHandles[0];	
		pkcs11.C_FindObjectsFinal(p11_session);


		// initialize the signature method
		CK_MECHANISM mechanism = new CK_MECHANISM();
		mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
		mechanism.pParameter = null;
		pkcs11.C_SignInit(p11_session, mechanism, signatureKey);


		// sign
		byte[] result = pkcs11.C_Sign(p11_session, data); 
		pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
		return  result;

	}


	public static void init() throws PteidException{
		System.out.println(" //Load the PTEidlibj");

		System.loadLibrary("pteidlibj");
		pteid.Init(""); // Initializes the eID Lib
		pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
		System.out.println(" //Loaded");
		
	}



	private static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException{
		CertificateFactory f = CertificateFactory.getInstance("X.509");
		InputStream in = new ByteArrayInputStream(certificateEncoded);
		X509Certificate cert = (X509Certificate)f.generateCertificate(in);
		return cert;
	}
	// Returns the n-th certificate, starting from 0
	private static  byte[] getCertificateInBytes(int n) {
		byte[] certificate_bytes = null;
		try {
			PTEID_Certif[] certs = pteid.GetCertificates();
			certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif
	
		} catch (PteidException e) {
			e.printStackTrace();
		}
		return certificate_bytes;
	}
}

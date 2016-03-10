package firstPart;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import Client.FSLib;
import junit.framework.TestCase;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
public class TestRunner{
	
	public static void main(String[] args) {
	      Result result = JUnitCore.runClasses(testsFSINIT.class,testsFSREAD.class,testsFSWRITE.class);
	      for (Failure failure : result.getFailures()) {
	         System.out.println(failure.toString());
	      }
	      System.out.println(result.wasSuccessful());
	   }
	
	
	
	
}




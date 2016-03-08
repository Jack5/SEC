package Server;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Vector;

public class Header implements Serializable  {

	private static final long serialVersionUID = 7219361879292051051L;
	
	public PublicKey pubKey;
	public byte[] signature;
	public Vector<String> ids;

	public Header(PublicKey pk, byte[] sig, Vector<String> newIds){
		pubKey = pk;
		signature = sig;
		ids = newIds;
	}
	
	
}

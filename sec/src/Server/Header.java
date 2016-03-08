package Server;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

public class Header implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7219361879292051051L;
	public Vector<String> ids;
	public Header(){
		ids = new Vector<String>();
	}
	public Header(Vector<String> newIds){
		ids = newIds;
	}
	
	
}

package Server;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

public class Header extends Block implements Serializable  {

	public Map<byte[],ContentBlock> hashes;
	
	
}

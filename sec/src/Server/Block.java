package Server;

import java.util.Vector;

public class Block {
	
	

	Vector<byte[]> pieces;
	
	void addPiece(){
		byte[] newPiece = new byte[2048];
		pieces.add(newPiece);
	}
	
	
	
}

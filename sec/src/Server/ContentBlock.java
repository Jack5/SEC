package Server;

import java.io.Serializable;

public class ContentBlock implements Serializable{
	
	private static final long serialVersionUID = 6555536570027771105L;
	public byte[] content;
	
	public ContentBlock(byte[] newContents){
		content = newContents;
	}
}

package chipmunk.nut;

import java.io.IOException;

import chipmunk.NativeChipmunk;

public class IOChipmunk extends NativeChipmunk {

	private static final long serialVersionUID = -3491699898212308165L;

	public IOChipmunk(IOException ex) {
		super(ex);
	}
	
	public IOChipmunk(String msg, IOException ex){
		super(msg, ex);
	}

}

package chipmunk.nut;

import chipmunk.AngryChipmunk;

public class MissingTypeChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = 8229356869225924938L;

	public MissingTypeChipmunk(){
		super();
	}
	
	public MissingTypeChipmunk(String msg){
		super(msg, null);
	}
	
}
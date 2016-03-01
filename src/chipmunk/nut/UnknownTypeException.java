package chipmunk.nut;

import chipmunk.AngryChipmunk;

public class UnknownTypeException extends AngryChipmunk {

	private static final long serialVersionUID = 8229356869225924938L;

	UnknownTypeException(){
		super();
	}
	
	UnknownTypeException(String msg){
		super(msg);
	}
	
}
package chipmunk.modules.lang;

import chipmunk.AngryChipmunk;

public class UnimplementedOperationException extends AngryChipmunk {

	private static final long serialVersionUID = 2415417246906136592L;

	public UnimplementedOperationException(){
		super();
	}
	
	public UnimplementedOperationException(String msg){
		super(msg);
	}
}

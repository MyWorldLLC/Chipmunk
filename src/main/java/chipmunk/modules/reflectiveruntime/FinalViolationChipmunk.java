package chipmunk.modules.reflectiveruntime;

import chipmunk.AngryChipmunk;

public class FinalViolationChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = -7540926854001205326L;

	public FinalViolationChipmunk(){
		super("You cannot overwrite a final variable once it has been set.");
	}
	
	public FinalViolationChipmunk(String message) {
		super(message);
	}
}

package chipmunk;

import java.util.ArrayDeque;

public class MissingVariableChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = -8159122710208246160L;

	
	public MissingVariableChipmunk(){
		this("");
	}
	
	public MissingVariableChipmunk(String message){
		super(message);
		traceFrames = new ArrayDeque<DebugInfo>();
	}
}

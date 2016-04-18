package chipmunk;

public class MissingVariableChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = -8159122710208246160L;

	
	public MissingVariableChipmunk(){
		this("", null);
	}
	
	public MissingVariableChipmunk(String msg){
		this(msg, null);
	}
	
	public MissingVariableChipmunk(String message, Throwable cause){
		super(message, cause);
	}
}

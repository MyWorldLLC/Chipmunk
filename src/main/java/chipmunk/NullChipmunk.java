package chipmunk;

public class NullChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = -5334774990948047966L;

	public NullChipmunk(){
		this("", null);
	}
	
	public NullChipmunk(String message){
		this(message, null);
	}
	
	public NullChipmunk(String message, Throwable cause){
		super(message, cause);
	}
}

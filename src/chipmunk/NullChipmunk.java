package chipmunk;

public class NullChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = -5334774990948047966L;

	public NullChipmunk(){
		this("");
	}
	
	public NullChipmunk(String message){
		super(message);
	}
}

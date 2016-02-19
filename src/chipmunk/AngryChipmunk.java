package chipmunk;

public class AngryChipmunk extends RuntimeException {

	private static final long serialVersionUID = 4997822014942264350L;

	public AngryChipmunk(){
		super();
	}
	
	public AngryChipmunk(String message){
		super(message);
	}
	
}

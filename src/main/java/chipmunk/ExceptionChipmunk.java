package chipmunk;

public class ExceptionChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = 1L;
	
	protected Object payload;
	
	public ExceptionChipmunk(Object payload){
		super();
		this.payload = payload;
	}
	
	public ExceptionChipmunk(Object payload, String msg){
		super(msg);
		this.payload = payload;
	}
	
	public void setPayload(Object payload){
		this.payload = payload;
	}
	
	public Object getPayload(){
		return payload;
	}

}

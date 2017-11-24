package chipmunk;

public class ExceptionChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = 1L;
	
	protected Object exceptionObj;
	
	public ExceptionChipmunk(Object exception){
		super();
		exceptionObj = exception;
	}
	
	public ExceptionChipmunk(Object exception, String msg){
		super(msg);
		exceptionObj = exception;
	}
	
	public Object getExceptionObject(){
		return exceptionObj;
	}

}

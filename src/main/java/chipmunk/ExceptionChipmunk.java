package chipmunk;

import chipmunk.modules.lang.CObject;

public class ExceptionChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = 1L;
	
	protected CObject exceptionObj;
	
	public ExceptionChipmunk(CObject exception){
		super();
		exceptionObj = exception;
	}
	
	public ExceptionChipmunk(CObject exception, String msg){
		super(msg);
		exceptionObj = exception;
	}
	
	public CObject getExceptionObject(){
		return exceptionObj;
	}

}

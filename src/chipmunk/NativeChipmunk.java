package chipmunk;

public class NativeChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = 4862801255426666976L;
	
	protected Exception nativeException;
	
	public NativeChipmunk(Exception ex){
		this("", ex);
	}
	
	public NativeChipmunk(String msg, Exception ex){
		super(msg, ex);
		nativeException = ex;
	}
	
	public Exception getNativeException(){
		return nativeException;
	}
}

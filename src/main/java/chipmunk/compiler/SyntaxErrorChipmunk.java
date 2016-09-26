package chipmunk.compiler;

public class SyntaxErrorChipmunk extends CompileChipmunk {

	private static final long serialVersionUID = 1758610427119118408L;

	public SyntaxErrorChipmunk(){
		super();
	}
	
	public SyntaxErrorChipmunk(String msg){
		super(msg);
	}
	
	public SyntaxErrorChipmunk(String msg, Throwable cause){
		super(msg, cause);
	}
	
}

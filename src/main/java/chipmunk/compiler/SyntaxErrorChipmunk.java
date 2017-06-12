package chipmunk.compiler;

public class SyntaxErrorChipmunk extends CompileChipmunk {

	private static final long serialVersionUID = 1758610427119118408L;
	
	private Token[] expected;
	private Token got;

	public SyntaxErrorChipmunk(){
		super();
	}
	
	public SyntaxErrorChipmunk(String msg){
		super(msg);
	}
	
	public SyntaxErrorChipmunk(String msg, Throwable cause){
		super(msg, cause);
	}

	public Token[] getExpected() {
		return expected;
	}

	public void setExpected(Token[] expected) {
		this.expected = expected;
	}

	public Token getGot() {
		return got;
	}

	public void setGot(Token got) {
		this.got = got;
	}
	
}

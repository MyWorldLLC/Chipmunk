package chipmunk.compiler;

import chipmunk.AngryChipmunk;

public class UnresolvedSymbolChipmunk extends AngryChipmunk {
	
	private static final long serialVersionUID = -3751232792577027254L;
	
	protected Token symbolName;
	
	public UnresolvedSymbolChipmunk(String msg, Token symbolName){
		super(msg);
		this.symbolName = symbolName;
	}
	
	
	public UnresolvedSymbolChipmunk(String msg, Token symbolName, Throwable cause){
		super(msg, cause);
		this.symbolName = symbolName;
	}
	
	public Token getSymbolName(){
		return symbolName;
	}
}

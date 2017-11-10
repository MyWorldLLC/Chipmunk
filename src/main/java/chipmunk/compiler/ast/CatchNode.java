package chipmunk.compiler.ast;

import chipmunk.compiler.Token;

public class CatchNode extends BlockNode {
	
	protected boolean hasExceptionName;
	protected Token exceptionType;
	
	public CatchNode(){
		hasExceptionName = false;
	}
	
	public void setExceptionName(Token name){
		if(name != null){
			if(hasExceptionName){
				children.remove(0);
			}
			hasExceptionName = true;
			children.add(0, new VarDecNode(new IdNode(name)));
		}
	}

}

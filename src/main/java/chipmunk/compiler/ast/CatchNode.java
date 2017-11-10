package chipmunk.compiler.ast;

import chipmunk.compiler.Token;

public class CatchNode extends BlockNode {
	
	protected boolean hasExceptionName;
	protected boolean hasExceptionType;
	
	protected Token exceptionType;
	
	public CatchNode(){
		hasExceptionName = false;
	}
	
	public void setExceptionName(Token name){
		if(hasExceptionName){
			if(hasExceptionType){
				children.remove(1);
			}else{
				children.remove(0);
			}
		}
		if(name != null){
			if(hasExceptionType){
				children.add(1, new VarDecNode(new IdNode(name)));
			}else{
				children.add(0, new VarDecNode(new IdNode(name)));
			}
			hasExceptionName = true;
		}else{
			hasExceptionName = false;
		}
	}
	
	public void setExceptionType(Token typeName){
		if(hasExceptionType){
			children.remove(0);
		}
		
		if(typeName != null){
			children.add(0, new IdNode(typeName));
			hasExceptionType = true;
		}else{
			hasExceptionType = false;
		}
	}
	
	public boolean hasExceptionName(){
		return hasExceptionName;
	}
	
	public boolean hasExceptionType(){
		return hasExceptionType;
	}
	
	public IdNode getExceptionType(){
		if(hasExceptionType){
			return (IdNode) children.get(0);
		}
		return null;
	}
	
	public VarDecNode getExceptionName(){
		if(hasExceptionName){
			if(hasExceptionType){
				return (VarDecNode) children.get(1);
			}else{
				return (VarDecNode) children.get(0);
			}
		}
		return null;
	}

}

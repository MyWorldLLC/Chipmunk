package chipmunk.compiler.ast;

import chipmunk.compiler.Token;

public class IdNode extends AstNode {
	
	protected Token id;
	
	public IdNode(){
		super();
	}
	
	public IdNode(Token id){
		super();
		this.id = id;
	}
	
	public Token getID(){
		return id;
	}
	
	public void setID(Token id){
		this.id = id;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("(id ");
		builder.append(id.getText());
		builder.append(')');
		
		return builder.toString();
	}

}

package chipmunk.compiler.ast;

import chipmunk.compiler.Token;

public class LiteralNode extends AstNode {

	protected Token literal;
	
	public LiteralNode(){
		super();
	}
	
	public LiteralNode(Token literalValue){
		super();
		literal = literalValue;
	}
	
	public Token getLiteral(){
		return literal;
	}
	
	public void setLiteral(Token literalValue){
		literal = literalValue;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("(literal ");
		builder.append(literal.toString());
		builder.append(')');
		
		return builder.toString();
	}
}

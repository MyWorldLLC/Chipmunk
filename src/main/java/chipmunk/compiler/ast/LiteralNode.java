package chipmunk.compiler.ast;

import chipmunk.compiler.Token;

public class LiteralNode extends AstNode {

	protected Token literal;
	
	public LiteralNode(){
		super();
	}
	
	public LiteralNode(Token literalValue){
		super();
		setLiteral(literalValue);
	}
	
	public Token getLiteral(){
		return literal;
	}
	
	public void setLiteral(Token literalValue){
		literal = literalValue;
		setLineNumber(literal.getLine());
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("(literal ");
		builder.append(literal.getText());
		builder.append(')');
		
		return builder.toString();
	}
}

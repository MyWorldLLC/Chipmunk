package chipmunk.compiler.ast;

import chipmunk.compiler.Token;

public class FlowControlNode extends AstNode {

	protected Token token;
	
	public FlowControlNode(){
		super();
	}
	
	public FlowControlNode(Token controlToken){
		super();
		token = controlToken;
	}
	
	public Token getControlToken(){
		return token;
	}
	
	public void setControlToken(Token controlToken){
		token = controlToken;
	}
	
	public void addControlExpression(AstNode expression){
		addChild(expression);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		builder.append(token.getText());
		builder.append(' ');
		
		for(AstNode child : children){
			builder.append(child.toString());
		}
		builder.append(')');
		return builder.toString();
	}
}

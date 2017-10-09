package chipmunk.compiler.ast;

import chipmunk.compiler.Token;

public class OperatorNode extends AstNode {
	
	protected Token op;
	
	public OperatorNode(Token op){
		super();
		this.op = op;
	}
	
	public OperatorNode(Token op, AstNode operand){
		this(op);
		addChild(operand);
	}
	
	public OperatorNode(Token op, AstNode lhs, AstNode rhs){
		this(op);
		addChild(lhs);
		addChild(rhs);
	}
	
	public Token getOperator(){
		return op;
	}
	
	public AstNode getLeft(){
		return children.size() > 0 ? children.get(0) : null;
	}
	
	public AstNode getRight(){
		return children.size() > 1 ? children.get(children.size() - 1) : null;
	}
	
	public void addOperand(AstNode operand){
		super.addChild(operand);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		builder.append(op.getText());
		builder.append(' ');
		
		for(AstNode child : children){
			builder.append(child.toString());
		}
		
		builder.append(')');
		return builder.toString();
	}

}

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

}

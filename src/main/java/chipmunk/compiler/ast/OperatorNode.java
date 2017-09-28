package chipmunk.compiler.ast;

import chipmunk.compiler.Operator;

public class OperatorNode extends AstNode {
	
	protected Operator op;
	
	public OperatorNode(){
		super();
	}
	
	public OperatorNode(Operator op){
		super();
		this.op = op;
	}
	
	public Operator getOperator(){
		return op;
	}
	
	public void setOperator(Operator op){
		this.op = op;
	}

}

package chipmunk.compiler.ast;

public class ExpressionNode extends AstNode {
	
	public ExpressionNode(){
		super();
	}
	
	public ExpressionNode(AstNode operand){
		super(operand);
	}
	
	public ExpressionNode(AstNode left, AstNode right){
		super(left, right);
	}

}

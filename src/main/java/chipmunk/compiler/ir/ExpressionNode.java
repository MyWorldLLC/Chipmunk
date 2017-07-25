package chipmunk.compiler.ir;

import chipmunk.compiler.Token;

public class ExpressionNode {
	
	protected Token token;
	
	protected ExpressionNode left;
	protected ExpressionNode right;
	
	public ExpressionNode(Token binaryOp, ExpressionNode left, ExpressionNode right){
		this.left = left;
		this.right = right;
	}
	
	public ExpressionNode(Token unaryOp, ExpressionNode left){
		this.left = left;
	}
	
	public ExpressionNode(Token literalOrID){
		this.token = literalOrID;
	}
	
	public Token getToken(){
		return token;
	}
	
	public boolean isTerminal(){
		return left == null && right == null;
	}
	
	public boolean isUnaryOp(){
		return right == null;
	}
	
	public boolean isBinaryOp(){
		return left != null && right != null;
	}

	public ExpressionNode getLeft() {
		return left;
	}

	public ExpressionNode getRight() {
		return right;
	}

}

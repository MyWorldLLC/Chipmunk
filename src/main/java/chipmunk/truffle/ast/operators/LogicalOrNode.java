package chipmunk.truffle.ast.operators;

import chipmunk.truffle.ast.ExpressionNode;

public class LogicalOrNode extends ShortCircuitNode {

	public LogicalOrNode(ExpressionNode left, ExpressionNode right) {
		super(left, right);
	}
	
	@Override
	public boolean shouldEvaluateRight(boolean leftValue) {
		return leftValue ? false : true;
	}

	@Override
	public boolean execute(boolean leftValue, boolean rightValue) {
		return leftValue || rightValue;
	}

}

package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

import chipmunk.truffle.TypeError;
import chipmunk.truffle.ast.ExpressionNode;

public abstract class ShortCircuitNode extends ExpressionNode {
	
	@Child
	private ExpressionNode left;
	@Child
	private ExpressionNode right;
	
	public ShortCircuitNode(ExpressionNode left, ExpressionNode right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public final Object executeGeneric(VirtualFrame frame) {
		return executeBoolean(frame);
	}
	
	public final boolean executeBoolean(VirtualFrame frame) {
		boolean leftValue;
		try {
			leftValue = left.executeBoolean(frame);
		} catch (UnexpectedResultException e) {
			throw new TypeError(this, e.getResult(), null);
		}
		
		boolean rightValue = false;
		if(shouldEvaluateRight(leftValue)) {
			try {
				rightValue = right.executeBoolean(frame);
			} catch (UnexpectedResultException e) {
				throw new TypeError(this, leftValue, e.getResult());
			}
		}
		
		return execute(leftValue, rightValue);
	}
	
	public abstract boolean shouldEvaluateRight(boolean leftValue);
	
	public abstract boolean execute(boolean leftValue, boolean rightValue);

}

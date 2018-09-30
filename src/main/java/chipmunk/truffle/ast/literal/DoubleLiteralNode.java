package chipmunk.truffle.ast.literal;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.ExpressionNode;

public class DoubleLiteralNode extends ExpressionNode {

	private final double value;
	
	public DoubleLiteralNode(double value) {
		this.value = value;
	}
	
	public double executeDouble(VirtualFrame frame) {
		return value;
	}
	
	public Object executeGeneric(VirtualFrame frame) {
		return value;
	}

}

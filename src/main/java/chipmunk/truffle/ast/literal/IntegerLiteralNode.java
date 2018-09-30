package chipmunk.truffle.ast.literal;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.ExpressionNode;

public class IntegerLiteralNode extends ExpressionNode {

	private final int value;
	
	public IntegerLiteralNode(int value) {
		this.value = value;
	}
	
	public int executeInteger(VirtualFrame frame) {
		return value;
	}
	
	public Object executeGeneric(VirtualFrame frame) {
		return value;
	}
}

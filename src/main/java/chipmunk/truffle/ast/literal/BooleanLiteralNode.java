package chipmunk.truffle.ast.literal;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.ExpressionNode;

public class BooleanLiteralNode extends ExpressionNode {

private final boolean value;
	
	public BooleanLiteralNode(boolean value) {
		this.value = value;
	}
	
	public boolean executeBoolean(VirtualFrame frame) {
		return value;
	}
	
	public Object executeGeneric(VirtualFrame frame) {
		return value;
	}

}

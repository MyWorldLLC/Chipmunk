package chipmunk.truffle.ast.literal;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.ExpressionNode;

public class FloatLiteralNode extends ExpressionNode {

private final float value;
	
	public FloatLiteralNode(float value) {
		this.value = value;
	}
	
	public float executeFloat(VirtualFrame frame) {
		return value;
	}
	
	public Object executeGeneric(VirtualFrame frame) {
		return value;
	}

}

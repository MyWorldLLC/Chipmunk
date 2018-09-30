package chipmunk.truffle.ast.literal;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.ExpressionNode;

public class LongLiteralNode extends ExpressionNode {

	private final long value;
	
	public LongLiteralNode(long value) {
		this.value = value;
	}
	
	public long executeLong(VirtualFrame frame) {
		return value;
	}
	
	public Object executeGeneric(VirtualFrame frame) {
		return value;
	}

}

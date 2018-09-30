package chipmunk.truffle.ast.literal;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.ExpressionNode;

public class StringLiteralNode extends ExpressionNode {

	private final String value;
	
	public StringLiteralNode(String value) {
		this.value = value;
	}
	
	public String executeString(VirtualFrame frame) {
		return value;
	}
	
	public Object executeGeneric(VirtualFrame frame) {
		return value;
	}
}

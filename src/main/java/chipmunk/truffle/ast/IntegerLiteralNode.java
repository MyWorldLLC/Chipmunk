package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

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

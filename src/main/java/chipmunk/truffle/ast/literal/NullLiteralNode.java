package chipmunk.truffle.ast.literal;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.runtime.Null;

public class NullLiteralNode extends ExpressionNode {
	
	public Object executeGeneric(VirtualFrame frame) {
		return Null.instance();
	}

}

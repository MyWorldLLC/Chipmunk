package chipmunk.truffle.ast.flow;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.StatementNode;

public class ThrowNode extends StatementNode {
	
	@Child
	protected ExpressionNode exp;
	
	public ThrowNode(ExpressionNode exp) {
		this.exp = exp;
	}

	@Override
	public void executeVoid(VirtualFrame frame) {
		// TODO - first-class support for chipmunk exceptions that
		// trace Chipmunk code
		throw (RuntimeException) exp.executeGeneric(frame);
	}

}

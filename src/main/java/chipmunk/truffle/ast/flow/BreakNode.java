package chipmunk.truffle.ast.flow;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.StatementNode;

public class BreakNode extends StatementNode {

	@Override
	public void executeVoid(VirtualFrame frame) {
		throw new BreakException();
	}

}

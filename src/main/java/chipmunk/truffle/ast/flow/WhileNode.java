package chipmunk.truffle.ast.flow;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.NodeInfo;

import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.StatementNode;

@NodeInfo(shortName="while")
public class WhileNode extends StatementNode {
	
	@Child
	private LoopNode loopBody;

	public WhileNode(ExpressionNode condition, StatementNode body) {
		loopBody = Truffle.getRuntime().createLoopNode(new WhileRepeatingNode(condition, body));
	}
	
	@Override
	public void executeVoid(VirtualFrame frame) {
		loopBody.executeLoop(frame);
	}

}

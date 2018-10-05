package chipmunk.truffle.ast.flow;

import java.util.Iterator;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.NodeInfo;

import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.StatementNode;

@NodeInfo(shortName="for")
public class ForNode extends StatementNode {
	
	@Child
	private LoopNode loopBody;
	
	@Child
	private ExpressionNode iter;
	
	@Child
	private StatementNode body;
	
	private final FrameSlot slot;

	public ForNode(FrameSlot slot, ExpressionNode iter, StatementNode body) {
		this.slot = slot;
		this.iter = iter;
		this.body = body;
	}
	
	@Override
	public void executeVoid(VirtualFrame frame) {
		@SuppressWarnings("unchecked")
		// TODO - should probably incorporate iterators into the type system
		Iterator<Object> iter = (Iterator<Object>) this.iter.executeGeneric(frame);
		// TODO - this may cause perf degredation. Explore ways to re-use branch profiling information
		loopBody = Truffle.getRuntime().createLoopNode(new ForRepeatingNode(slot, iter, body));
		loopBody.executeLoop(frame);
	}

}

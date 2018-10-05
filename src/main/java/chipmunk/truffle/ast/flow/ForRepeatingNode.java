package chipmunk.truffle.ast.flow;

import java.util.Iterator;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.profiles.BranchProfile;

import chipmunk.truffle.ast.StatementNode;

public class ForRepeatingNode extends Node implements RepeatingNode {
	
	private Iterator<Object> iter;
	
	@Child
	private StatementNode body;
	
	private final BranchProfile continueProfile;
	private final BranchProfile breakProfile;
	
	private final FrameSlot slot;
	
	public ForRepeatingNode(FrameSlot slot, Iterator<Object> iter, StatementNode body)
	{
		this.slot = slot;
		this.iter = iter;
		this.body = body;
		
		continueProfile = BranchProfile.create();
		breakProfile = BranchProfile.create();
	}

	@Override
	public boolean executeRepeating(VirtualFrame frame) {
		
		if(!evaluateIteration(frame)) {
			return false;
		}
		
		try {
			body.executeVoid(frame);
			return true;
		}catch(ContinueException e) {
			continueProfile.enter();
			return true;
		}catch(BreakException e) {
			breakProfile.enter();
			return false;
		}
	}
	
	private boolean evaluateIteration(VirtualFrame frame) {
		if(iter.hasNext())
		{
			frame.setObject(slot, iter.next());
			return true;
		}else {
			return false;
		}
	}

}

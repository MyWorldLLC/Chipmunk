package chipmunk.truffle.ast.flow;

import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.BranchProfile;

import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.StatementNode;

public class WhileRepeatingNode extends Node implements RepeatingNode {
	
	@Child
	private ExpressionNode condition;
	
	@Child
	private StatementNode body;
	
	private final BranchProfile continueProfile;
	private final BranchProfile breakProfile;
	
	public WhileRepeatingNode(ExpressionNode condition, StatementNode body)
	{
		this.condition = condition;
		this.body = body;
		
		continueProfile = BranchProfile.create();
		breakProfile = BranchProfile.create();
	}

	@Override
	public boolean executeRepeating(VirtualFrame frame) {
		
		if(!evaluateCondition(frame)) {
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
	
	private boolean evaluateCondition(VirtualFrame frame) {
		try {
			return condition.executeBoolean(frame);
		}catch(UnexpectedResultException e) {
			throw new UnsupportedSpecializationException(this, new Node[] {condition}, e.getResult());
		}
	}

}

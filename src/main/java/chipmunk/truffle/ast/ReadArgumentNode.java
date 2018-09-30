package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadArgumentNode extends ExpressionNode {
	
	private final int index;
	
	public ReadArgumentNode(int index) {
		this.index = index;
	}

	@Override
	public Object executeGeneric(VirtualFrame frame) {
		return frame.getArguments()[index];
	}

}

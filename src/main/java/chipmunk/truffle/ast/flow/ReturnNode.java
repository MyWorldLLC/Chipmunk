package chipmunk.truffle.ast.flow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.StatementNode;

@NodeInfo(shortName="return")
public class ReturnNode extends StatementNode {

	@Child
	private ExpressionNode value;
	
	public ReturnNode(ExpressionNode value) {
		this.value = value;
	}
	
	@Override
	public void executeVoid(VirtualFrame frame) {
		Object result = null;
		
		if(value != null) {
			result = value.executeGeneric(frame);
		}
		
		throw new ReturnException(result);
	}

}

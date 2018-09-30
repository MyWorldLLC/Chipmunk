package chipmunk.truffle.ast;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName="block")
public class BlockNode extends StatementNode {
	
	@Children
	private StatementNode[] body;
	
	public BlockNode(StatementNode[] bodyNodes) {
		body = bodyNodes;
	}
	
	@ExplodeLoop
	@Override
	public void executeVoid(VirtualFrame frame) {
		CompilerAsserts.compilationConstant(body.length);
		
		for(int i = 0; i < body.length; i++) {
			body[i].executeVoid(frame);
		}
	}

}

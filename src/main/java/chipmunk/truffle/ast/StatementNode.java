package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(language="Chipmunk", description="The base node for all Chipmunk statements")
public abstract class StatementNode extends Node {
	
	public abstract void executeVoid(VirtualFrame frame);
	
}

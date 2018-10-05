package chipmunk.truffle.ast.flow;

import chipmunk.truffle.ast.BlockNode;
import chipmunk.truffle.ast.StatementNode;

public class IfElseNode extends BlockNode {

	public IfElseNode(StatementNode[] bodyNodes) {
		super(bodyNodes);
	}

}

package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;

public class GotoNode extends OpNode {
	
	private final int target;

	public GotoNode(ChipmunkVM vm, OpNode[] nodes, int index, int next) {
		super(vm, nodes, index);
		target = next;
	}

	@Override
	public OpNode executeAndGetNext(VirtualFrame frame) throws Throwable {
		return nodes[target];
	}

}

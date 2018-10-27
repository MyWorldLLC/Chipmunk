package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;

public class PopNode extends StaticOpNode {

	public PopNode(ChipmunkVM vm, OpNode[] nodes, int index) {
		super(vm, nodes, index);
	}

	@Override
	public void exec(VirtualFrame frame) throws Throwable {
		vm.pop();
	}

}

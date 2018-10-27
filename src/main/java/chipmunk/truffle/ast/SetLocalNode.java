package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;

public class SetLocalNode extends StaticOpNode {
	
	private final FrameSlot localIndex;

	public SetLocalNode(ChipmunkVM vm, OpNode[] nodes, int index, FrameSlot localIndex) {
		super(vm, nodes, index);
		this.localIndex = localIndex;
	}
	
	@Override
	public void exec(VirtualFrame frame) throws Throwable {
		frame.setObject(localIndex, vm.peek());
	}

}

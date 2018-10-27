package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;

public class GetLocalNode extends OpNode {

	protected final FrameSlot localIndex;
	
	public GetLocalNode(ChipmunkVM vm, OpNode[] nodes, int index, FrameSlot localSlot) {
		super(vm, nodes, index);
		this.localIndex = localSlot;
	}

	@Override
	public OpNode executeAndGetNext(VirtualFrame frame) throws Throwable {
		
		vm.push(frame.getObject(localIndex));
		
		return nodes[index + 1];
	}

}

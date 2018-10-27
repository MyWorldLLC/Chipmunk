package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;

public class PushNode extends StaticOpNode {
	
	private final Object[] constantPool;
	private final int constantIndex;

	public PushNode(ChipmunkVM vm, OpNode[] nodes, int index, Object[] constantPool, int constantPoolIndex) {
		super(vm, nodes, index);
		this.constantPool = constantPool;
		constantIndex = constantPoolIndex;
	}

	@Override
	public void exec(VirtualFrame frame) throws Throwable {
		vm.push(constantPool[constantIndex]);
	}

}

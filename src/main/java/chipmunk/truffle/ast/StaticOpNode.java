package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;

public abstract class StaticOpNode extends OpNode {
	
	@Child
	protected OpNode next;
	
	public StaticOpNode(ChipmunkVM vm, OpNode[] nodes, int index) {
		super(vm, nodes, index);
	}
	
	public ChipmunkVM getVM() {
		return vm;
	}
	
	public void link() {
		next = nodes[index + 1];
	}
	
	public OpNode executeAndGetNext(VirtualFrame frame) throws Throwable {
		exec(frame);
		return next;
	}
	
	public abstract void exec(VirtualFrame frame) throws Throwable;

}

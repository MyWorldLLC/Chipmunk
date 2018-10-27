package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;

import chipmunk.ChipmunkVM;

@NodeInfo(language="Chipmunk", shortName="operator")
public abstract class OpNode extends Node {

	protected final ChipmunkVM vm;
	
	protected final OpNode[] nodes;
	protected final int index;
	
	@Child
	protected OpNode next;
	
	public OpNode(ChipmunkVM vm, OpNode[] nodes, int index) {
		this.vm = vm;
		this.nodes = nodes;
		this.index = index;
		
		next = nodes[index];
	}
	
	public ChipmunkVM getVM() {
		return vm;
	}
	
	public abstract OpNode executeAndGetNext(VirtualFrame frame) throws Throwable;
	
}

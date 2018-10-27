package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;
import chipmunk.modules.runtime.CBoolean;

public class IfNode extends OpNode {

	private final int falseIndex;
	private final Object[] params;
	private final OpDispatcher dispatch;
	
	public IfNode(ChipmunkVM vm, OpNode[] nodes, int index, int falseIndex) {
		super(vm, nodes, index);
		this.falseIndex = falseIndex;
		
		params = new Object[1];
		params[0] = vm;
		
		dispatch = new OpDispatcher(vm, "truth");
	}

	@Override
	public OpNode executeAndGetNext(VirtualFrame frame) throws Throwable {
		Object ins = vm.pop();
		
		boolean value = ((CBoolean) dispatch.dispatch(ins, params)).booleanValue();
		if(!value) {
			return nodes[falseIndex];
		}else {
			return nodes[index + 1];
		}
	}

}

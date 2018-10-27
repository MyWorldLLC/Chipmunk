package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;

public class AddNode extends StaticOpNode {
	
	private final Object[] params;
	private final OpDispatcher dispatch;

	public AddNode(ChipmunkVM vm, OpNode[] nodes, int index) {
		super(vm, nodes, index);
		params = new Object[2];
		params[0] = vm;
		
		dispatch = new OpDispatcher(vm, "plus");
	}

	@Override
	public void exec(VirtualFrame frame) throws Throwable {
		params[1] = vm.pop();
		Object left = vm.pop();
		
		vm.push(dispatch.dispatch(left, params));
	}

}

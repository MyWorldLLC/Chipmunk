package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;
import chipmunk.modules.runtime.CBoolean;
import chipmunk.modules.runtime.CInteger;

public class LessThanNode extends StaticOpNode {

	private final Object[] params;
	private final OpDispatcher dispatch;
	
	public LessThanNode(ChipmunkVM vm, OpNode[] nodes, int index) {
		super(vm, nodes, index);
		params = new Object[2];
		params[0] = vm;
		
		dispatch = new OpDispatcher(vm, "compare");
	}

	@Override
	public void exec(VirtualFrame frame) throws Throwable {
		params[1] = vm.pop();
		Object left = vm.pop();
		
		Object value = dispatch.dispatch(left, params);
		
		if (((CInteger) value).getValue() < 0) {
			vm.push(new CBoolean(true));
		} else {
			vm.push(new CBoolean(false));
		}
	}

}

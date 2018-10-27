package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.ChipmunkVM;
import chipmunk.truffle.ast.flow.ReturnException;

public class ReturnNode extends OpNode {

	public ReturnNode(ChipmunkVM vm, OpNode[] nodes, int index) {
		super(vm, nodes, index);
	}

	@Override
	public OpNode executeAndGetNext(VirtualFrame frame) throws Throwable {
		throw new ReturnException(vm.pop());
	}

}

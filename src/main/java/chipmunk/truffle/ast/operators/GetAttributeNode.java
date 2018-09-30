package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.frame.VirtualFrame;

import chipmunk.truffle.ast.BinaryOpNode;

public abstract class GetAttributeNode extends BinaryOpNode {

	@Override
	public Object executeGeneric(VirtualFrame frame) {
		// TODO
		throw new UnsupportedOperationException();
	}

}

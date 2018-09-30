package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.UnaryOpNode;

public abstract class NotNode extends UnaryOpNode {

	@Specialization
	public boolean doBoolean(boolean value) {
		return !value;
	}

}

package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.UnaryOpNode;

public abstract class PosNode extends UnaryOpNode {

	@Specialization
	public int doInteger(int value) {
		return Math.abs(value);
	}
	
}

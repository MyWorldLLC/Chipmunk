package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.BinaryOpNode;

public abstract class DivNode extends BinaryOpNode {

	@Specialization
	public float doIntegers(int left, int right) {
		return left / (float) right;
	}

}

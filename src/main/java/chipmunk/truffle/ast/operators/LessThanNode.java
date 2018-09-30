package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.BinaryOpNode;

public abstract class LessThanNode extends BinaryOpNode {

	@Specialization
	public boolean compareIntegers(int left, int right) {
		return left < right;
	}

}

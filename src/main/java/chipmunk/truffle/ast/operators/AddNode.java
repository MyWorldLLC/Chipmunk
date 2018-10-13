package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.BinaryOpNode;

public abstract class AddNode extends BinaryOpNode {

	@Specialization
	public int addIntegers(int left, int right) {
		return left + right;
	}

}
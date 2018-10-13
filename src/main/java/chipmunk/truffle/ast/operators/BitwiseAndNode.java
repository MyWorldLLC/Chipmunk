package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.BinaryOpNode;

public abstract class BitwiseAndNode extends BinaryOpNode {

	@Specialization
	public int doIntegers(int left, int right) {
		return left & right;
	}

}
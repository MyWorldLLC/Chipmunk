package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.BinaryOpNode;

public abstract class PowerNode extends BinaryOpNode {

	@Specialization
	public int doIntegers(int left, int right) {
		return (int) Math.pow(left, right);
	}
}

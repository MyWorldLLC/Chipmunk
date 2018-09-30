package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.BinaryOpNode;

public abstract class LogicalGreaterThanNode extends BinaryOpNode {

	@Specialization
	public boolean doIntegers(int left, int right) {
		return left > right;
	}
}

package chipmunk.truffle.ast.operators;

import com.oracle.truffle.api.dsl.Specialization;

import chipmunk.truffle.ast.BinaryOpNode;

public abstract class GetAtNode extends BinaryOpNode {

	@Specialization
	public Object doInteger(Object[] options, int index) {
		return options[index];
	}

}

package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class AddSubOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.ADD_SUB;
	}

}

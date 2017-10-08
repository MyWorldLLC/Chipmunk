package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class EqualityOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.EQUAL_NEQUAL;
	}

}

package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class BitXOrOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.BITXOR;
	}

}

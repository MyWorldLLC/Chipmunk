package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class BitAndOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.BITAND;
	}

}

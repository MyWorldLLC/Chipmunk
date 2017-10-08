package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class BitOrOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.BITOR;
	}

}

package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class MulDivOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.MULT_DIV_MOD;
	}

}

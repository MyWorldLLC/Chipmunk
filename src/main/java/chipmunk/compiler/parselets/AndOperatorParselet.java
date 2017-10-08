package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class AndOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.AND;
	}

}

package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class OrOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.OR;
	}

}

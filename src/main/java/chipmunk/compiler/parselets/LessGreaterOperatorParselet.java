package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class LessGreaterOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.LESS_GREATER_THAN;
	}

}

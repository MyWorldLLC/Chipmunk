package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class AssignOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.ASSIGN;
	}

}

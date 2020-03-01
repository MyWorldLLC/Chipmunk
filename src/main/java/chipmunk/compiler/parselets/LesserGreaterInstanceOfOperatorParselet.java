package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class LesserGreaterInstanceOfOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.LESSER_GREATER_THAN_INSTANCE_OF;
	}

}

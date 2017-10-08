package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class DotOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.DOT_INDEX_CALL;
	}

}

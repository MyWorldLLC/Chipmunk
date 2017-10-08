package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class AssignOperatorParselet extends BaseBinaryOperatorParselet {
	
	public AssignOperatorParselet(){
		super(false);
	}

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.ASSIGN;
	}

}

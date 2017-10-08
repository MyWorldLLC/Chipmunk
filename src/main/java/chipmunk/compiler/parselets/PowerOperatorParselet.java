package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class PowerOperatorParselet extends BaseBinaryOperatorParselet {
	
	public PowerOperatorParselet(){
		super(false);
	}

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.POW;
	}

}

package chipmunk.compiler.parselets;

import chipmunk.compiler.OperatorPrecedence;

public class ShiftRangeOperatorParselet extends BaseBinaryOperatorParselet {

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.SHIFT_L_R_RANGE;
	}

}

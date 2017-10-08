package chipmunk.compiler;

public class OperatorPrecedence {
	
	public static final int DOT_INDEX_CALL = 13;
	public static final int POW = 12;
	public static final int POST_INC_DEC = 11;
	public static final int PRE_INC_DEC = 10;
	public static final int POS_NEG_NOT_BNOT = 10;
	public static final int MULT_DIV_MOD = 10;
	public static final int ADD_SUB = 9;
	public static final int SHIFT_L_R_RANGE = 8;
	public static final int LESS_GREATER_THAN = 7;
	public static final int EQUAL_NEQUAL = 6;
	public static final int BITAND = 5;
	public static final int BITXOR = 4;
	public static final int BITOR = 3;
	public static final int AND = 2;
	public static final int OR = 1;
	public static final int ASSIGN = 0;
}

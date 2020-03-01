package chipmunk.compiler;

public class OperatorPrecedence {
	
	public static final int DOT_INDEX_CALL = 14;
	public static final int POW = 13;
	public static final int POST_INC_DEC = 12;
	public static final int PRE_OP = 11;
	public static final int MULT_DIV_MOD = 11;
	public static final int ADD_SUB = 10;
	public static final int SHIFT_L_R_RANGE = 9;
	public static final int LESSER_GREATER_THAN_INSTANCE_OF = 8;
	public static final int EQUAL_NEQUAL = 7;
	public static final int BITAND = 6;
	public static final int BITXOR = 5;
	public static final int BITOR = 4;
	public static final int AND = 3;
	public static final int OR = 2;
	public static final int ASSIGN = 1;
}

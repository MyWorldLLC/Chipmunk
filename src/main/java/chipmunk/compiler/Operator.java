package chipmunk.compiler;

import static chipmunk.compiler.Token.Type.*;

public enum Operator {
	
	DOTOP(0, DOT),
	POW(1, STAR, STAR),
	POSTINC(2, PLUS, PLUS), POSTDEC(2, MINUS, MINUS),
	PREINC(3, PLUS, PLUS), PREDEC(3, MINUS, MINUS), POS(3, PLUS), NEG(3, MINUS), NOT(3, EXCLAMATION), BNOT(3, TILDE),
	MULT(3, STAR), DIV(3, FSLASH), FDIV(3, FSLASH, FSLASH), MOD(3, PERCENT),
	ADD(4, PLUS), SUB(4, MINUS),
	LSHIFT(5, LESSTHAN, LESSTHAN), RSHIFT(5, MORETHAN, MORETHAN), URSHIFT(5, MORETHAN, MORETHAN, MORETHAN), RANGE(5, DOT, DOT),
	LESS(6, LESSTHAN), GREATER(6, MORETHAN), LESSEQUAL(6, LESSTHAN, EQUALS), MOREEQUAL(6, MORETHAN, EQUALS), ASOP(6, AS), 
	EQUAL(7, EQUALS, EQUALS), NOTEQUAL(7, EXCLAMATION, EQUALS),
	BITAND(8, AMPERSAND),
	BITXOR(9, CARET),
	BITOR(10, BAR),
	AND(11, AMPERSAND, AMPERSAND),
	OR(12, BAR, BAR),
	ASSIGN(13, EQUALS), ADDASSIGN(13, PLUS, EQUALS), SUBASSIGN(13, MINUS, EQUALS), MULASSIGN(13, STAR, EQUALS),
	DIVASSIGN(13, FSLASH, EQUALS), MODASSIGN(13, PERCENT, EQUALS), BITANDASSIGN(13, AMPERSAND, EQUALS), BITXORASSIGN(13, CARET, EQUALS),
	BITORASSIGN(13, BAR, EQUALS), LSHIFTASSIGN(13, LESSTHAN, LESSTHAN, EQUALS), RSHIFTASSIGN(13, MORETHAN, MORETHAN),
	URSHIFTASSIGN(13, MORETHAN, MORETHAN, MORETHAN, EQUALS);
	
	private int precedence;
	private Token.Type[] tokens;
	
	private Operator(int precedence, Token.Type... tokens){
		this.precedence = precedence;
		this.tokens = tokens;
	}
	
	public int getPrecedence(){
		return precedence;
	}
	
	public Token.Type[] getTokenSequence(){
		return tokens;
	}
	
	public static Operator match(Token.Type... tokens){
		Operator[] operators = Operator.values();
		
		for(int i = 0; i < operators.length; i++){
			
			Operator op = operators[i];
			
			if(op.tokens.length == tokens.length){
				
				boolean mismatch = false;
				for(int tok = 0; tok < op.tokens.length; tok++){
					
					if(op.tokens[tok] != tokens[tok]){
						mismatch = true;
						break;
					}
				}
				
				if(!mismatch){
					return op;
				}
			}
		}
		return null;
	}
}

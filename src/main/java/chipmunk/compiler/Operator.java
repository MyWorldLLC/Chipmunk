package chipmunk.compiler;

import static chipmunk.compiler.Token.Type.*;

public enum Operator {
	
	DOTOP(0, false, DOT), OPENINDEX(0, true, LBRACKET), CLOSEINDEX(0, true, RBRACKET), OPENCALL(0, true, LPAREN), CLOSECALL(0, true, RPAREN),
	POW(1, false, STAR, STAR),
	POSTINC(2, true, PLUS, PLUS), POSTDEC(2, true, MINUS, MINUS),
	PREINC(3, true, PLUS, PLUS), PREDEC(3, true, MINUS, MINUS), POS(3, true, PLUS), NEG(3, true, MINUS), NOT(3, true, EXCLAMATION), BNOT(3, true, TILDE),
	MULT(3, false, STAR), DIV(3, false, FSLASH), FDIV(3, false, FSLASH, FSLASH), MOD(3, false, PERCENT),
	ADD(4, false, PLUS), SUB(4, false, MINUS),
	LSHIFT(5, false, LESSTHAN, LESSTHAN), RSHIFT(5, false, MORETHAN, MORETHAN), URSHIFT(5, false, MORETHAN, MORETHAN, MORETHAN), RANGE(5, false, DOT, DOT),
	LESS(6, false, LESSTHAN), GREATER(6, false,  MORETHAN), LESSEQUAL(6, false, LESSTHAN, EQUALS), MOREEQUAL(6, false, MORETHAN, EQUALS), ASOP(6, false, AS), 
	EQUAL(7, false, EQUALS, EQUALS), NOTEQUAL(7, false, EXCLAMATION, EQUALS),
	BITAND(8, false, AMPERSAND),
	BITXOR(9, false, CARET),
	BITOR(10, false, BAR),
	AND(11, false, AMPERSAND, AMPERSAND),
	OR(12, false, BAR, BAR),
	ASSIGN(13, false, EQUALS), ADDASSIGN(13, false, PLUS, EQUALS), SUBASSIGN(13, false, MINUS, EQUALS), MULASSIGN(13, false, STAR, EQUALS),
	DIVASSIGN(13, false, FSLASH, EQUALS), MODASSIGN(13, false, PERCENT, EQUALS), BITANDASSIGN(13, false, AMPERSAND, EQUALS), BITXORASSIGN(13, false, CARET, EQUALS),
	BITORASSIGN(13, false, BAR, EQUALS), LSHIFTASSIGN(13, false, LESSTHAN, LESSTHAN, EQUALS), RSHIFTASSIGN(13, false, MORETHAN, MORETHAN),
	URSHIFTASSIGN(13, false, MORETHAN, MORETHAN, MORETHAN, EQUALS);
	
	private int precedence;
	private boolean unary;
	private Token.Type[] tokens;
	
	private Operator(int precedence, boolean unary, Token.Type... tokens){
		this.precedence = precedence;
		this.unary = unary;
		this.tokens = tokens;
	}
	
	public int getPrecedence(){
		return precedence;
	}
	
	public boolean isUnary(){
		return unary;
	}
	
	public boolean isBinary(){
		return !unary;
	}
	
	public Token.Type[] getTokenSequence(){
		return tokens;
	}
	
	public static Operator match(Token.Type...tokens){
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
	
	private static Operator match(boolean unary, Token.Type... tokens){
		Operator[] operators = Operator.values();
		
		for(int i = 0; i < operators.length; i++){
			
			Operator op = operators[i];
			
			if(unary == op.isUnary() && op.tokens.length == tokens.length){
				
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
	
	public static Operator matchUnary(Token.Type... tokens){
		return match(true, tokens);
	}
	
	public static Operator matchBinary(Token.Type... tokens){
		return match(false, tokens);
	}
}

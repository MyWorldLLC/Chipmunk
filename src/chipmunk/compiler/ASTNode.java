package chipmunk.compiler;

public class ASTNode {
	
	public enum Type {
		ADD, SUB, MUL, DIV, FDIV, EXP, MOD, GETATTR, SETATTR, GETAT, SETAT, ASSIGN, LOOP, IF, ELSE,
		TRY, EXCEPT, CLASSDEF, METHODDEF, METHODCALL
	}

	protected Token[] tokens;
	
	public ASTNode(Token[] t){
		tokens = t;
	}
	
	public Token[] getTokens(){
		return tokens;
	}
	
	public void setTokens(Token[] t){
		tokens = t;
	}
}

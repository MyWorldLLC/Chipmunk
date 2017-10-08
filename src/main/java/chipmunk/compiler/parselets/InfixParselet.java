package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;

/**
 * One of two parselets used by Pratt parser. Used to post infix expressions and postfix
 * operators.
 */
public interface InfixParselet {

	public AstNode parse(ChipmunkParser parser, AstNode left, Token token);
	public int getPrecedence();
	
}

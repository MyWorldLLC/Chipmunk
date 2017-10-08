package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;

/**
 * One of two base parselets used by Pratt parser. Used to parse prefix expressions.
 */
public interface PrefixParselet {
	
	public AstNode parse(ChipmunkParser parser, Token token);

}

package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;

public class GroupingParselet implements PrefixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, Token token) {
		AstNode expr = parser.parseExpression();
		parser.forceNext(Token.Type.RPAREN);
		return expr;
	}

}

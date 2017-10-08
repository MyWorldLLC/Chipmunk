package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.IdNode;

public class NameParselet implements PrefixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, Token token) {
		return new IdNode(token);
	}

}

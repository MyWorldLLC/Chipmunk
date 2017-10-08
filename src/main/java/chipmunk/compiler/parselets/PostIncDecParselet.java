package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.OperatorPrecedence;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.OperatorNode;

public class PostIncDecParselet implements InfixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, AstNode left, Token token) {
		return new OperatorNode(token, left);
	}

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.POST_INC_DEC;
	}

}

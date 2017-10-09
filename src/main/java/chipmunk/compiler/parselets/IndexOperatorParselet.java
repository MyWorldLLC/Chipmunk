package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.OperatorPrecedence;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.OperatorNode;

public class IndexOperatorParselet implements InfixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, AstNode left, Token token) {
		OperatorNode node = new OperatorNode(token, left, parser.parseExpression());
		parser.forceNext(Token.Type.RBRACKET);
		return node;
	}

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.DOT_INDEX_CALL;
	}

}

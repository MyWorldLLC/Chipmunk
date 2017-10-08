package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.OperatorPrecedence;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;

public class IndexOperatorParselet implements InfixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, AstNode left, Token token) {
		AstNode node = new AstNode();
		
		node.addChild(parser.parseExpression());
		
		parser.forceNext(Token.Type.RBRACKET);
		return node;
	}

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.DOT_INDEX_CALL;
	}

}

package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.OperatorPrecedence;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.OperatorNode;

public class CallOperatorParselet implements InfixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, AstNode left, Token token) {
		
		OperatorNode node = new OperatorNode(token, left);
		
		while(parser.getTokens().peek().getType() != Token.Type.RPAREN){
			AstNode arg = parser.parseExpression();
			node.addChild(arg);
			
			if(parser.peek(Token.Type.COMMA)){
				parser.dropNext();
			}
		}
		parser.forceNext(Token.Type.RPAREN);
		
		return node;
	}

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.DOT_INDEX_CALL;
	}

}

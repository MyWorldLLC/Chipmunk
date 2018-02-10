package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.Token;
import chipmunk.compiler.TokenStream;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.ListNode;

public class ListParselet implements PrefixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, Token token) {
		ListNode list = new ListNode();
		
		TokenStream tokens = parser.getTokens();
		while(!parser.peek(Token.Type.RBRACKET)){
			
			list.addChild(parser.parseExpression());
			parser.skipNewlines();
			
			if(!(parser.dropNext(Token.Type.COMMA) || parser.peek(Token.Type.RBRACKET))){
				parser.syntaxError("Error parsing list", tokens.peek(), Token.Type.COMMA, Token.Type.RBRACKET);
			}
		}
		return list;
	}

}

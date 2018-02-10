package chipmunk.compiler.parselets;

import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.Token;
import chipmunk.compiler.TokenStream;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.MapNode;

public class MapParselet implements PrefixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, Token token) {
		MapNode map = new MapNode();
		
		TokenStream tokens = parser.getTokens();
		while(tokens.peek().getType() != Token.Type.RBRACE){
			
			AstNode key = parser.parseExpression();
			parser.forceNext(Token.Type.COLON);
			AstNode value = parser.parseExpression();
			
			map.addMapping(key, value);
			
			parser.skipNewlines();
			
			if(!(parser.dropNext(Token.Type.COMMA) || parser.peek(Token.Type.RBRACE))){
				parser.syntaxError("Error parsing map", tokens.peek(), Token.Type.COMMA, Token.Type.RBRACE);
			}
		}
		return map;
	}

}

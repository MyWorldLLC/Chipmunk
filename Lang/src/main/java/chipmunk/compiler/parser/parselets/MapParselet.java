/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.compiler.parser.parselets;

import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.compiler.parser.ExpressionParser;

public class MapParselet implements PrefixParselet {

	@Override
	public AstNode parse(ExpressionParser parser, Token token) {
		AstNode map = new AstNode(NodeType.MAP, token);
		
		TokenStream tokens = parser.getTokens();
		while(tokens.peek().type() != TokenType.RBRACE){

			tokens.skipNewlinesAndComments();

			if(!tokens.peek(TokenType.RBRACE)){
				AstNode key = parser.parseExpression();

				tokens.skipNewlinesAndComments();

				var colon = tokens.getNext(TokenType.COLON);

				tokens.skipNewlinesAndComments();

				AstNode value = parser.parseExpression();

				map.addChild(new AstNode(NodeType.KEY_VALUE, colon).withChild(key).withChild(value));
			}

			tokens.skipNewlinesAndComments();
			
			if(!(tokens.dropNext(TokenType.COMMA) || tokens.peek(TokenType.RBRACE))){
				ChipmunkParser.syntaxError("Error parsing map", tokens.getFileName(), tokens.peek(), TokenType.COMMA, TokenType.RBRACE);
			}
		}
		tokens.dropNext(TokenType.RBRACE);
		return map;
	}

}

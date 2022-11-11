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

import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.ListNode;

public class ListParselet implements PrefixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, Token token) {
		ListNode list = new ListNode();
		
		TokenStream tokens = parser.getTokens();
		while(!parser.peek(TokenType.RBRACKET)){

			parser.skipNewlinesAndComments();
			list.addChild(parser.parseExpression());
			parser.skipNewlinesAndComments();
			
			if(!(parser.dropNext(TokenType.COMMA) || parser.peek(TokenType.RBRACKET))){
				parser.syntaxError("Error parsing list", tokens.peek(), TokenType.COMMA, TokenType.RBRACKET);
			}
		}
		parser.dropNext(TokenType.RBRACKET);
		return list;
	}

}

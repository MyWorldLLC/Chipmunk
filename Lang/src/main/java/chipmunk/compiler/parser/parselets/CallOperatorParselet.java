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

import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.OperatorPrecedence;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.compiler.parser.ExpressionParser;

public class CallOperatorParselet implements InfixParselet {

	@Override
	public AstNode parse(ExpressionParser parser, AstNode left, Token token) {
		
		OperatorNode node = new OperatorNode(token, left);

		TokenStream tokens = parser.getTokens();
		while(tokens.peek().type() != TokenType.RPAREN){
			AstNode arg = parser.parseExpression();
			node.addOperand(arg);
			
			if(tokens.peek(TokenType.COMMA)){
				tokens.dropNext();
			}
		}
		tokens.forceNext(TokenType.RPAREN);
		
		return node;
	}

	@Override
	public int getPrecedence() {
		return OperatorPrecedence.DOT_BIND_INDEX_CALL;
	}

}

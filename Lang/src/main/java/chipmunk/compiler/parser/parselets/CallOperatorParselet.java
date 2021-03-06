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

import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.compiler.OperatorPrecedence;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.OperatorNode;

public class CallOperatorParselet implements InfixParselet {

	@Override
	public AstNode parse(ChipmunkParser parser, AstNode left, Token token) {
		
		OperatorNode node = new OperatorNode(token, left);
		
		while(parser.getTokens().peek().getType() != Token.Type.RPAREN){
			AstNode arg = parser.parseExpression();
			node.addOperand(arg);
			
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

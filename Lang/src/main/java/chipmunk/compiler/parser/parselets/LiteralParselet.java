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
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ExpressionParser;
import chipmunk.compiler.types.ObjectType;

public class LiteralParselet implements PrefixParselet {

	@Override
	public AstNode parse(ExpressionParser parser, Token token) {
		var node = new AstNode(NodeType.LITERAL, token);
		switch (token.type()){
			case BINARYLITERAL, OCTLITERAL, HEXLITERAL, INTLITERAL -> node.setResultType(ObjectType.primitive("int"));
			case FLOATLITERAL -> node.setResultType(ObjectType.primitive("float"));
			case BOOLLITERAL -> node.setResultType(ObjectType.primitive("boolean"));
			case STRINGLITERAL -> node.setResultType(ObjectType.classBased("string"));
		}
		return node;
	}

}

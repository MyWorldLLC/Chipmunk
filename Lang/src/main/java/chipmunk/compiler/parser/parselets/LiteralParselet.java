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
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.FloatType;
import chipmunk.compiler.types.IntegerType;

public class LiteralParselet implements PrefixParselet {

	@Override
	public AstNode parse(ExpressionParser parser, Token token) {
		var node = new AstNode(NodeType.LITERAL, token);
		switch (token.type()){
			case BINARYLITERAL, OCTLITERAL, HEXLITERAL, INTLITERAL -> node.setResultType(intTypeOf(token.text()));
			case FLOATLITERAL -> node.setResultType(floatTypeOf(token.text()));
			case BOOLLITERAL -> node.setResultType(BuiltinTypes.BOOLEAN);
			case STRINGLITERAL -> node.setResultType(BuiltinTypes.STRING);
		}
		return node;
	}

	public static IntegerType intTypeOf(String literal){
		var ending = literal.charAt(literal.length() - 1);
		if(!Character.isAlphabetic(ending)){
			return IntegerType.INT;
		}

		return switch (Character.toLowerCase(ending)){
			case 'b' -> IntegerType.BYTE;
			case 's' -> IntegerType.SHORT;
			case 'i' -> IntegerType.INT;
			case 'l' -> IntegerType.LONG;
			default -> throw new IllegalArgumentException("Invalid int qualifier: " + ending);
		};
	}

	public static FloatType floatTypeOf(String literal){
		var ending = literal.charAt(literal.length() - 1);
		if(!Character.isAlphabetic(ending)){
			return FloatType.FLOAT;
		}

		return switch (Character.toLowerCase(ending)){
			case 'f' -> FloatType.FLOAT;
			case 'd' -> FloatType.DOUBLE;
			default -> throw new IllegalArgumentException("Invalid float qualifier: " + ending);
		};
	}

	public static String stripQualifier(String literal){
		var ending = literal.charAt(literal.length() - 1);
		if(Character.isAlphabetic(ending)){
			return literal.substring(0, literal.length() - 1);
		}
		return literal;
	}

	public static String stripRadixQualifier(String literal){
		if(literal.length() < 2){
			return literal;
		}
		var beginning = literal.substring(0, 2).toLowerCase();
		return switch (beginning){
			case "0b", "0o", "0x" -> literal.substring(2);
			default -> literal;
		};
	}

	public static int radix(String literal){
		if(TokenType.BINARYLITERAL.getPattern().matcher(literal).matches()){
			return 2;
		}else if(TokenType.OCTLITERAL.getPattern().matcher(literal).matches()){
			return 8;
		}else if(TokenType.HEXLITERAL.getPattern().matcher(literal).matches()){
			return 16;
		}else{
			return 10;
		}
	}

}

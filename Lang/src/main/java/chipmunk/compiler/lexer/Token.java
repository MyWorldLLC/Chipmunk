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

package chipmunk.compiler.lexer;

public record Token(String text, TokenType type, int index, int line, int column) {

	public static final int UNKNOWN = -1;

	public Token(String token, TokenType tokenType){
		this(token, tokenType, UNKNOWN, UNKNOWN, UNKNOWN);
	}

	public Token(String token, TokenType tokenType, int line){
		this(token, tokenType, UNKNOWN, line, UNKNOWN);
	}

	public Token(String token, TokenType tokenType, int line, int column){
		this(token, tokenType, UNKNOWN, line, column);
	}

	public boolean isSynthetic(){
		return index == UNKNOWN;
	}

	public Token withLine(int line){
		return new Token(text, type, index, line, column);
	}
	
	@Override
	public String toString(){
		return text.trim() + "(" + type.name().toLowerCase() + ")";
	}

	public static int lineOrNone(Token t){
		return t != null ? t.line() : UNKNOWN;
	}

	public static int columnOrNone(Token t){
		return t != null ? t.column() : UNKNOWN;
	}

}

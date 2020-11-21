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

package chipmunk.compiler;

import chipmunk.compiler.lexer.Token;

public class SyntaxError extends CompileChipmunk {

	private static final long serialVersionUID = 1758610427119118408L;
	
	private Token.Type[] expected;
	private Token got;

	public SyntaxError(){
		super();
	}
	
	public SyntaxError(String msg){
		super(msg);
	}
	
	public SyntaxError(String msg, Token got){
		super(msg);
		this.got = got;
	}
	
	public SyntaxError(String msg, Throwable cause){
		super(msg, cause);
	}

	public Token.Type[] getExpected() {
		return expected;
	}

	public void setExpected(Token.Type[] expected) {
		this.expected = expected;
	}

	public Token getGot() {
		return got;
	}

	public void setGot(Token got) {
		this.got = got;
	}
	
}

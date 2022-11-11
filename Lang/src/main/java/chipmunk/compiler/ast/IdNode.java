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

package chipmunk.compiler.ast;

import chipmunk.compiler.lexer.Token;

public class IdNode extends AstNode {
	
	protected Token id;
	
	public IdNode(){
		super();
	}
	
	public IdNode(Token id){
		super();
		setID(id);
	}

	public IdNode(String id){
		super();
		setID(new Token(id, Token.Type.IDENTIFIER));
	}
	
	public Token getID(){
		return id;
	}

	public String getName(){
		return id.text();
	}
	
	public void setID(Token id){
		this.id = id;
		setLineNumber(id.line());
	}

	@Override
	public String getDebugName(){
		return "id " + id.text();
	}

}

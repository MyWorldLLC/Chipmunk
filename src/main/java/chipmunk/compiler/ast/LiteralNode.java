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

public class LiteralNode extends AstNode {

	protected Token literal;
	
	public LiteralNode(){
		super();
	}
	
	public LiteralNode(Token literalValue){
		super();
		setLiteral(literalValue);
	}
	
	public Token getLiteral(){
		return literal;
	}
	
	public void setLiteral(Token literalValue){
		literal = literalValue;
		setLineNumber(literal.getLine());
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("(literal ");
		builder.append(literal.getText());
		builder.append(')');
		
		return builder.toString();
	}
}

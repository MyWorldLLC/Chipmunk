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

import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;

public class ClassNode extends BlockNode implements SymbolNode {
	
	protected Symbol symbol;
	
	public ClassNode(){
		super(SymbolTable.Scope.CLASS);
		symbol = new Symbol();
	}
	
	public ClassNode(String name){
		this();
		setName(name);
	}

	public String getName(){
		return symbol.getName();
	}

	public void setName(String name){
		symbol.setName(name);
		getSymbolTable().setDebugSymbol(name);
	}

	public void addChild(AstNode child){
		super.addChild(child);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("(class ");
		builder.append(symbol.getName());
		
		for(AstNode child : children){
			builder.append(' ');
			builder.append(child.toString());
		}
		
		builder.append(")");
		return builder.toString();
	}

	@Override
	public Symbol getSymbol() {
		return symbol;
	}
}

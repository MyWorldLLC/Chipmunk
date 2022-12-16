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

import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;

public class MethodNode extends BlockNode {
	
	protected int defaultParamCount;
	protected int paramCount;
	
	public MethodNode(){
		super(NodeType.METHOD, SymbolTable.Scope.METHOD);
		symbol = new Symbol();
		defaultParamCount = 0;
		paramCount = 0;
	}
	
	public MethodNode(String name){
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
	
	public int getParamCount(){
		return paramCount;
	}
	
	public boolean hasParams(){
		return paramCount != 0;
	}
	
	public int getDefaultParamCount(){
		return defaultParamCount;
	}
	
	public boolean hasDefaultParams(){
		return defaultParamCount != 0;
	}
	
	public void addParam(VarDecNode param){
		addParam(paramCount, param);
	}

	public void addParam(int index, VarDecNode param){
		if(index > paramCount){
			throw new IllegalArgumentException(String.format("Parameter index %d greater than parameter count %d"));
		}

		children.add(paramCount, param);
		paramCount++;

		if(param.getAssignExpr() != null){
			defaultParamCount++;
		}
	}
	
	public void addToBody(AstNode node){
		addChild(node);
	}

	@Override
	public String getDebugName(){
		return "method " + symbol.getName();
	}

}

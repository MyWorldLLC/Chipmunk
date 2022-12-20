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

public class ModuleNode extends BlockNode {

	protected String fileName;
	
	public ModuleNode(){
		super(NodeType.MODULE, SymbolTable.Scope.MODULE);
		symbol = new Symbol();
	}
	
	public ModuleNode(String name){
		this();
		setName(name);
	}
	
	public void setName(String name) {
		symbol.setName(name);
		getSymbolTable().setDebugSymbol(name);
	}
	
	public String getName() {
		return symbol.getName();
	}

	public String getFileName(){
		return fileName;
	}

	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public void addImport(AstNode node){
		addChild(node);
	}
	
	public void addClassDef(ClassNode node){
		addChild(node);
	}
	
	public void addVarDec(VarDecNode node){
		addChild(node);
	}
	
	public void addMethodDef(MethodNode node){
		addChild(node);
	}

	@Override
	public String getDebugName(){
		return "module " + symbol.getName();
	}

}

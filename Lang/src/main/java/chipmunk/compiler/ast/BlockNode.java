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

import chipmunk.compiler.symbols.SymbolTable;

public class BlockNode extends AstNode {
	
	protected SymbolTable symTab;
	
	public BlockNode(NodeType type){
		super(type);
		symTab = new SymbolTable();
		symTab.setNode(this);
	}
	
	public BlockNode(NodeType type, SymbolTable.Scope scope){
		super(type);
		symTab = new SymbolTable(scope);
		symTab.setNode(this);
	}
	
	public BlockNode(NodeType type, AstNode... children){
		super(type, null, children);
		symTab = new SymbolTable();
		symTab.setNode(this);
	}
	
	public BlockNode(NodeType type, SymbolTable.Scope scope, AstNode... children){
		super(type, null, children);
		symTab = new SymbolTable(scope);
	}
	
	public SymbolTable getSymbolTable(){
		return symTab;
	}
	
	public void setParentSymbolTable(BlockNode node){
		symTab.setParent(node.getSymbolTable());
	}
	
	public void addToBody(AstNode node){
		super.addChild(node);
	}
	
	public void addToBody(AstNode... nodes){
		super.addChildren(nodes);
	}

}

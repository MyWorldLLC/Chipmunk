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

package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.BlockNode;
import chipmunk.compiler.ast.CatchNode;
import chipmunk.compiler.ast.ImportNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.SymbolNode;

public class SymbolTableBuilderVisitor implements AstVisitor {
	
	protected SymbolTable currentScope;

	@Override
	public void visit(AstNode node) {
		
		if(node instanceof SymbolNode){
			if(currentScope != null){
				Symbol symbol = ((SymbolNode) node).getSymbol();
				// only set non-empty symbols - otherwise superfluous local slots are created for
				// anonymous methods/classes
				if(!symbol.getName().equals("")) {
					currentScope.setSymbol(symbol);
				}
			}
		}
		
		if(node instanceof BlockNode){
			BlockNode block = (BlockNode) node;
			SymbolTable blockTable = block.getSymbolTable();
			blockTable.setParent(currentScope);
			
			if(node instanceof MethodNode){
				blockTable.setSymbol(new Symbol("self", true));
			}

			currentScope = blockTable;
			
		}
		
		if(node instanceof ImportNode){
			ImportNode importNode = (ImportNode) node;
			
			List<String> symbols = new ArrayList<String>();
			
			if(importNode.hasAliases()){
				symbols = importNode.getAliases();
			}else{
				symbols = importNode.getSymbols();
			}
			
			for(String symbol : symbols){
				currentScope.setSymbol(new Symbol(symbol, true));
			}
		}
		
		node.visitChildren(this);
		
		if(currentScope != null && node instanceof BlockNode){
			currentScope = currentScope.getParent();
		}
	}

}

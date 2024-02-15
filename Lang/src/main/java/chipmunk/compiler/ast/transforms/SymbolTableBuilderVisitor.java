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

package chipmunk.compiler.ast.transforms;

import chipmunk.compiler.ast.*;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;
import chipmunk.compiler.symbols.SymbolType;

public class SymbolTableBuilderVisitor implements AstVisitor {
	
	protected SymbolTable currentScope;

	@Override
	public void visit(AstNode node) {

		if(node.is(NodeType.IMPORT)){
			return; // Don't visit import nodes - they must be handled specially by the ImportResolverVisitor
		}
		
		if(node.getSymbol() != null){
			if(currentScope != null){
				Symbol symbol = node.getSymbol();
				// only set non-empty symbols - otherwise superfluous local slots are created for
				// anonymous methods/classes
				if(!symbol.getName().equals("")) {
					currentScope.setSymbol(symbol);
				}
			}

			if(node.getNodeType() == NodeType.CLASS){
				node.getSymbol().setType(SymbolType.CLASS);
			}else if(node.getNodeType() == NodeType.METHOD){
				node.getSymbol().setType(SymbolType.METHOD);
			}else if(node.getNodeType() == NodeType.VAR_DEC){
				node.getSymbol().setType(SymbolType.VAR);
			}

		}
		
		if(node.getNodeType().isBlock()){
			SymbolTable blockTable = node.getSymbolTable();
			blockTable.setParent(currentScope);
			
			if(node.getNodeType() == NodeType.METHOD){
				blockTable.setSymbol(new Symbol("self", true));
			}

			currentScope = blockTable;
			
		}

		node.visitChildren(this);
		
		if(currentScope != null && node.getNodeType().isBlock()){
			currentScope = currentScope.getParent();
		}
	}

}

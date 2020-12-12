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
		
		if(node instanceof SymbolNode){
			SymbolNode symbolNode = (SymbolNode) node;
			if(currentScope != null){
				Symbol symbol = symbolNode.getSymbol();
				// only set non-empty symbols - otherwise superfluous local slots are created for
				// anonymous methods/classes
				if(!symbol.getName().equals("")) {
					currentScope.setSymbol(symbol);
				}
			}

			if(symbolNode instanceof ClassNode){
				symbolNode.getSymbol().setType(SymbolType.CLASS);
			}else if(symbolNode instanceof MethodNode){
				symbolNode.getSymbol().setType(SymbolType.METHOD);
			}else if(symbolNode instanceof VarDecNode){
				symbolNode.getSymbol().setType(SymbolType.VAR);
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

		node.visitChildren(this);
		
		if(currentScope != null && node instanceof BlockNode){
			currentScope = currentScope.getParent();
		}
	}

}

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

import chipmunk.compiler.Symbol;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.SymbolNode;
import chipmunk.compiler.ast.VarDecNode;

public class InnerMethodRewriteVisitor implements AstVisitor {
	
	private int nestingDepth;
	
	public InnerMethodRewriteVisitor() {
		nestingDepth = 0;
	}

	@Override
	public void visit(AstNode node) {

		for(int i = 0; i < node.getChildren().size(); i++) {
			AstNode nextChild = node.getChildren().get(i);
			final boolean rewrite = nextChild instanceof MethodNode && !((MethodNode) nextChild).getName().equals("") && nestingDepth > 0;
			
			
			if(nextChild instanceof MethodNode) {
				nestingDepth++;
			}
			
			nextChild.visit(this);
			
			if(rewrite) {
				VarDecNode dec = new VarDecNode();
				Symbol symbol = ((SymbolNode) nextChild).getSymbol();
				dec.getSymbol().setName(symbol.getName());
				dec.setVar(new IdNode(new Token(symbol.getName(), Token.Type.IDENTIFIER)));
				dec.setAssignExpr(nextChild);
				node.getChildren().set(i, dec);
				symbol.setName("");
			}
				
			if(nextChild instanceof MethodNode) {
				nestingDepth--;
			}
		}
	}

}

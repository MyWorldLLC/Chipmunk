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

import chipmunk.compiler.assembler.ChipmunkAssembler;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ForNode;
import chipmunk.compiler.ast.VarDecNode;

public class ForVisitor implements AstVisitor {

	private ChipmunkAssembler assembler;
	private Codegen codegen;
	
	public ForVisitor(Codegen codegen){
		this.codegen = codegen;
		assembler = codegen.getAssembler();
	}
	
	@Override
	public void visit(AstNode node) {
		if(node instanceof ForNode){
			ForNode loop = (ForNode) node;
			
			SymbolTable symbols = loop.getSymbolTable();
			LoopLabels labels = codegen.pushLoop();
			
			assembler.setLabelTarget(labels.getStartLabel());
			
			VarDecNode id = loop.getID();
			id.getSymbol().setFinal(true);
			assembler.onLine(id.getLineNumber());
			
			codegen.enterScope(symbols);
			
			// visit iterator expression and push the iterator
			loop.getIter().visit(new ExpressionVisitor(codegen));
			assembler.iter();
			
			// the "next" bytecode operates as the guard in the for loop
			assembler.setLabelTarget(labels.getGuardLabel());
			assembler.next(labels.getEndLabel());
			
			// set the next value in the iterator as a local variable
			assembler.setLocal(symbols.getLocalIndex(id.getVarName()));
			//assembler.pop();
			
			assembler.closeLine();

			// generate body
			loop.visitChildren(codegen, 2);
			codegen.exitScope();
			
			// jump to iterator
			assembler._goto(labels.getGuardLabel());
			
			// set end label target
			assembler.setLabelTarget(labels.getEndLabel());
			// pop the iterator
			assembler.pop();
			
			codegen.exitLoop();
		}
	}

}

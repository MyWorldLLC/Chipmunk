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
import chipmunk.compiler.ast.*;
import chipmunk.compiler.symbols.SymbolTable;

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

			IteratorNode iter = loop.getIterator();

			codegen.enterScope(symbols);
			
			// Visit iterator expression and push the iterator
			iter.getIter().visit(new ExpressionVisitor(codegen));
			assembler.iter();
			assembler.setLocal(symbols.getLocalIndex(iter.getSymbol()));

			assembler._goto(labels.getGuardLabel());

			// Mark the start of the body
			assembler.setLabelTarget(labels.getStartLabel());

			// Generate body
			loop.visitChildren(codegen, 1);
			codegen.exitScope();

			// Generate the guard - the "next" bytecode operates as the guard
			VarDecNode id = iter.getID();
			id.getSymbol().setFinal(true);
			assembler.onLine(id.getLineNumber());
			assembler.setLabelTarget(labels.getGuardLabel());

			assembler.getLocal(symbols.getLocalIndex(iter.getSymbol()));
			assembler.callAt("hasNext", (byte)0);
			assembler._if(labels.getEndLabel());

			assembler.getLocal(symbols.getLocalIndex(iter.getSymbol()));
			assembler.callAt("next", (byte) 0);
			
			// Set the next value returned by the iterator as a local variable
			assembler.setLocal(symbols.getLocalIndex(id.getVarName()));
			
			assembler.closeLine();
			
			// Jump to iterator
			assembler._goto(labels.getStartLabel());
			
			// Set end label target
			assembler.setLabelTarget(labels.getEndLabel());

			// Pop the iterator
			//assembler.pop();
			
			codegen.exitLoop();
		}
	}

}

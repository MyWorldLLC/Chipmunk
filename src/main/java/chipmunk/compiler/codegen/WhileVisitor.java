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
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.WhileNode;

public class WhileVisitor implements AstVisitor {

	private ChipmunkAssembler assembler;
	private Codegen codegen;
	
	public WhileVisitor(Codegen codegen){
		this.codegen = codegen;
		assembler = codegen.getAssembler();
	}
	
	@Override
	public void visit(AstNode node) {
		if(node instanceof WhileNode){
			WhileNode loop = (WhileNode) node;
			
			LoopLabels labels = codegen.pushLoop();
			
			assembler.setLabelTarget(labels.getStartLabel());
			assembler.setLabelTarget(labels.getGuardLabel());
			
			loop.getGuard().visit(new ExpressionVisitor(codegen));
			assembler.closeLine();
			
			// if guard does not evaluate true, jump to end
			assembler._if(labels.getEndLabel());
			
			codegen.enterScope(loop.getSymbolTable());
			// generate body
			loop.visitChildren(codegen, 1);
			codegen.exitScope();
			
			// jump to guard
			assembler._goto(labels.getGuardLabel());
			
			// set end label target
			assembler.setLabelTarget(labels.getEndLabel());
			
			codegen.exitLoop();
		}
	}

}

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

public class IfElseVisitor implements AstVisitor {
	
	private ChipmunkAssembler assembler;
	private Codegen codegen;
	
	public IfElseVisitor(Codegen codegen){
		this.codegen = codegen;
		assembler = codegen.getAssembler();
	}

	@Override
	public void visit(AstNode node) {
		if(node.is(NodeType.IF_ELSE)){
			IfElseLabels endLabels = codegen.pushIfElse();
			
			node.visitChildren(this);
			
			// label the end of the if/else
			assembler.setLabelTarget(endLabels.getEndLabel());
			codegen.exitIfElse();
		}else if(node.is(NodeType.IF)){
			node.getLeft().visit(new ExpressionVisitor(codegen));
			
			String endOfIf = assembler.nextLabelName();
			// go to end of this node's body if the if does not evaluate true
			assembler._if(endOfIf);
			
			// generate code for the children, skipping the guard statement
			codegen.enterScope(node.getSymbolTable());
			node.visitChildren(codegen, 1);
			
			// go to the end of the entire if/else if body executes
			assembler._goto(codegen.peekClosestIfElse().getEndLabel());
			// mark end of the if block
			assembler.setLabelTarget(endOfIf);
			
		}else if(node.is(NodeType.ELSE)){
			// else branch
			node.visitChildren(codegen);
		}
	}

}

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

import chipmunk.binary.ExceptionBlock;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.CatchNode;
import chipmunk.compiler.ast.FinallyNode;
import chipmunk.compiler.ast.TryCatchNode;
import chipmunk.compiler.ast.TryNode;

public class TryCatchVisitor implements AstVisitor {

	protected Codegen codegen;
	
	public TryCatchVisitor(Codegen codegen) {
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		if(node instanceof TryCatchNode) {
			TryCatchLabels labels = codegen.pushTryCatch();
			
			node.visitChildren(this);
			
			// Build exception table
			ExceptionBlock block = new ExceptionBlock();
			block.startIndex = codegen.getAssembler().getLabelTarget(labels.start);
			block.catchIndex = codegen.getAssembler().getLabelTarget(labels.getCatchBlocks().get(0).getStartLabel()); // TODO - multicatch/finally
			block.exceptionLocalIndex = ((CatchBlock)labels.getCatchBlocks().get(0)).exceptionLocalIndex;
			block.endIndex = codegen.getAssembler().getLabelTarget(labels.end);
			
			codegen.addExceptionBlock(block);
			codegen.exitTryCatch();
		}else if(node instanceof TryNode) {
			codegen.getAssembler().setLabelTarget(codegen.peekClosestTryCatch().getStartLabel());
		
			// Assemble try body
			codegen.enterScope(((TryNode)node).getSymbolTable());
			node.visitChildren(codegen);
			codegen.exitScope();
			
			codegen.getAssembler().setLabelTarget(codegen.peekClosestTryCatch().getEndLabel());
		}else if(node instanceof CatchNode) {
			CatchNode catchNode = (CatchNode) node;
			
			CatchBlock catchLabels = new CatchBlock(codegen.getAssembler().nextLabelName(), codegen.getAssembler().nextLabelName());
			codegen.peekClosestTryCatch().getCatchBlocks().add(catchLabels);
			
			codegen.getAssembler().setLabelTarget(catchLabels.getStartLabel());
			
			// Assemble catch body
			codegen.enterScope(catchNode.getSymbolTable());
			
			catchLabels.exceptionLocalIndex = catchNode.getSymbolTable().getLocalIndex(catchNode.getExceptionName().getSymbol());
			
			node.visitChildren(codegen);
			codegen.exitScope();
			
			codegen.getAssembler().setLabelTarget(catchLabels.getEndLabel());
		}else if(node instanceof FinallyNode) {
			BlockLabels finallyLabels = new BlockLabels(codegen.getAssembler().nextLabelName(), codegen.getAssembler().nextLabelName());
			codegen.peekClosestTryCatch().getCatchBlocks().add(finallyLabels);
			
			codegen.getAssembler().setLabelTarget(finallyLabels.getStartLabel());
			
			// Assemble finally body
			codegen.enterScope(((FinallyNode)node).getSymbolTable());
			node.visitChildren(codegen);
			codegen.exitScope();
			
			codegen.getAssembler().setLabelTarget(finallyLabels.getEndLabel());
		}

	}

}

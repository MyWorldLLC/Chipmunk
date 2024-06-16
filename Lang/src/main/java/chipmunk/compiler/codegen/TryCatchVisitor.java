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
import chipmunk.compiler.ast.*;

public class TryCatchVisitor implements AstVisitor {

	protected Codegen codegen;
	
	public TryCatchVisitor(Codegen codegen) {
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		if(node.getNodeType() == NodeType.TRY_CATCH) {
			TryCatchLabels labels = codegen.pushTryCatch();
			
			node.visitChildren(this);

			// If no finally block exists, set the success target
			// (this will run right after the last catch block has been assembled)
			if(!labels.isSuccessMarked()){
				setSuccessTarget(labels);
			}

			// Build exception table
			ExceptionBlock block = new ExceptionBlock();
			block.startIndex = codegen.getAssembler().getLabelTarget(labels.start);
			block.catchIndex = codegen.getAssembler().getLabelTarget(labels.getCatchBlocks().get(0).getStartLabel()); // TODO - multicatch/finally
			block.exceptionLocalIndex = ((CatchBlock)labels.getCatchBlocks().get(0)).exceptionLocalIndex;
			block.endIndex = codegen.getAssembler().getLabelTarget(labels.end);
			
			codegen.addExceptionBlock(block);
			codegen.exitTryCatch();

		}else if(node.getNodeType() == NodeType.TRY) {
			TryCatchLabels labels = codegen.peekClosestTryCatch();
			codegen.getAssembler().setLabelTarget(labels.getStartLabel());
		
			// Assemble try body
			codegen.enterScope(node.getSymbolTable());
			node.visitChildren(codegen);
			codegen.exitScope();

			codegen.getAssembler()._goto(labels.getSuccessTarget());
			codegen.getAssembler().setLabelTarget(labels.getEndLabel());
		}else if(node.getNodeType() == NodeType.CATCH) {
			
			CatchBlock catchLabels = new CatchBlock(codegen.getAssembler().nextLabelName(), codegen.getAssembler().nextLabelName());
			codegen.peekClosestTryCatch().getCatchBlocks().add(catchLabels);
			
			codegen.getAssembler().setLabelTarget(catchLabels.getStartLabel());
			
			// Assemble catch body
			codegen.enterScope(node.getSymbolTable());

			// Get exception off top of stack & store to the exception local
			catchLabels.exceptionLocalIndex = node.getSymbolTable().getLocalIndex(node.getChild().getSymbol());
			//codegen.getAssembler().setLocal(catchLabels.exceptionLocalIndex);
			
			node.visitChildren(codegen);
			codegen.exitScope();
			
			codegen.getAssembler().setLabelTarget(catchLabels.getEndLabel());

		}else if(node.getNodeType() == NodeType.FINALLY) {
			TryCatchLabels tryCatch = codegen.peekClosestTryCatch();
			FinallyBlock finallyLabels = new FinallyBlock(codegen.getAssembler().nextLabelName(), codegen.getAssembler().nextLabelName());
			tryCatch.getCatchBlocks().add(finallyLabels);

			setSuccessTarget(tryCatch);

			codegen.getAssembler().setLabelTarget(finallyLabels.getStartLabel());
			
			// Assemble finally body
			codegen.enterScope((node).getSymbolTable());
			node.visitChildren(codegen);
			codegen.exitScope();
			
			codegen.getAssembler().setLabelTarget(finallyLabels.getEndLabel());
		}

	}

	protected void setSuccessTarget(TryCatchLabels labels){
		codegen.getAssembler().setLabelTarget(labels.getSuccessTarget());
		labels.setSuccessMarked();
	}

}

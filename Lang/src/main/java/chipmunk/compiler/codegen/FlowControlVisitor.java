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
import chipmunk.compiler.CompileChipmunk;
import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.lexer.TokenType;

public class FlowControlVisitor implements AstVisitor {
	
	protected Codegen codegen;
	protected ChipmunkAssembler assembler;
	
	public FlowControlVisitor(Codegen codegen){
		this.codegen = codegen;
		assembler = codegen.getAssembler();
	}

	@Override
	public void visit(AstNode node) {
		if(node.is(NodeType.FLOW_CONTROL)){
			
			Token token = node.getToken();
			assembler.onLine(token.line());
			
			if(token.type() == TokenType.RETURN){
				if(node.hasChildren()){
					//node.visitChildren(new ExpressionVisitor(codegen));
				}else{
					assembler.pushNull();
				}
				assembler._return();
			}else if(token.type() == TokenType.THROW){
				//node.visitChildren(new ExpressionVisitor(codegen));
				assembler._throw();
			}else if(token.type() == TokenType.BREAK){
				if(!codegen.inLoop()){
					//throw new CompileChipmunk(String.format("Invalid break at %s: %d: Can only break when inside a loop", token.getFile(), token.getLine()));
					throw new CompileChipmunk(String.format("Invalid break at %d: Can only break when inside a loop", token.line()));
	
				}
				assembler._goto(codegen.peekClosestLoop().getEndLabel());
			}else if(token.type() == TokenType.CONTINUE){
				if(!codegen.inLoop()){
					//throw new CompileChipmunk(String.format("Invalid continue at %s: %d: Can only continue when inside a loop", token.getFile(), token.getLine()));
					throw new CompileChipmunk(String.format("Invalid continue at %d: Can only continue when inside a loop", token.line()));
				}
				assembler._goto(codegen.peekClosestLoop().getGuardLabel());
			}
			assembler.closeLine();
			return;
		}
	}

}

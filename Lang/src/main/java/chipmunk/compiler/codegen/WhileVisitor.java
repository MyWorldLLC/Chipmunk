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
import chipmunk.compiler.ast.NodeType;
import chipmunk.vm.tree.Node;
import chipmunk.vm.tree.nodes.Block;
import chipmunk.vm.tree.nodes.FunctionNode;
import chipmunk.vm.tree.nodes.While;

import java.util.Arrays;

public class WhileVisitor implements CodegenVisitor {

	private Codegen codegen;
	
	public WhileVisitor(Codegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public Node visit(AstNode node) {
		if(node.getNodeType() == NodeType.WHILE){
			
			//LoopLabels labels = codegen.pushLoop();

			// Jump past the body, enter the guard
			//assembler._goto(labels.getGuardLabel());

			// Mark the start of the body
			//assembler.setLabelTarget(labels.getStartLabel());

			codegen.enterScope(node.getSymbolTable());
			// Generate body

			var test = new ExpressionVisitor(codegen).visit(node.getChild(0));

			var body = new Node[node.childCount() - 1];
			for(int i = 1; i < node.childCount(); i++){
				body[i - 1] = codegen.visit(node.getChild(i));
			}

			var whileNode = new While();
			whileNode.test = test;
			whileNode.body = new Block(body);
			//node.visitChildren(codegen, 1);
			codegen.exitScope();

			System.out.println(node.childCount());
			System.out.println(Arrays.toString(body));
			System.out.println("made while node with body size " + body.length);
			return whileNode;

			// Mark and generate the guard
			//assembler.setLabelTarget(labels.getGuardLabel());
			//node.getChild().visit(new ExpressionVisitor(codegen));
			//assembler.closeLine();
			
			// If guard evaluates false, jump to end
			//assembler._if(labels.getEndLabel());
			
			// Jump back to start if guard evaluates true
			//assembler._goto(labels.getStartLabel());
			
			// set end label target
			//assembler.setLabelTarget(labels.getEndLabel());
			
			//codegen.exitLoop();
		}
		return null;
	}

}

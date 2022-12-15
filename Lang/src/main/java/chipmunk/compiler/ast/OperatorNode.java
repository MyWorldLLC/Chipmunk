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

package chipmunk.compiler.ast;

import chipmunk.compiler.lexer.Token;

public class OperatorNode extends AstNode {
	
	protected Token op;
	
	public OperatorNode(Token op){
		super();
		setOperator(op);
	}
	
	public OperatorNode(Token op, AstNode operand){
		this(op);
		addChild(operand);
	}
	
	public OperatorNode(Token op, AstNode lhs, AstNode rhs){
		this(op);
		addChild(lhs);
		addChild(rhs);
	}
	
	public Token getOperator(){
		return op;
	}
	
	public void setOperator(Token op) {
		this.op = op;
	}
	
	public AstNode getLeft(){
		return children.size() > 0 ? children.get(0) : null;
	}
	
	public AstNode getRight(){
		return children.size() > 1 ? children.get(children.size() - 1) : null;
	}
	
	public void addOperand(AstNode operand){
		super.addChild(operand);
	}

	@Override
	public String getDebugName(){
		return op.text();
	}

}

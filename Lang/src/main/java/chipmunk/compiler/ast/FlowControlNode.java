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

public class FlowControlNode extends AstNode {

	protected Token token;
	
	public FlowControlNode(){
		super();
	}
	
	public FlowControlNode(Token controlToken){
		super();
		setControlToken(controlToken);
	}
	
	public Token getControlToken(){
		return token;
	}
	
	public void setControlToken(Token controlToken){
		token = controlToken;
		setLineNumber(controlToken.getLine());
	}
	
	public void addControlExpression(AstNode expression){
		addChild(expression);
	}

	@Override
	public String getDebugName(){
		return token.getText();
	}

}

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

import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.lexer.Token;

public class VarDecNode extends AstNode {
	
	protected boolean hasVar;
	protected boolean hasAssignExpr;

	public VarDecNode(){
		this((Token)null);
	}

	public VarDecNode(Token origin){
		super(NodeType.VAR_DEC, origin);
		hasVar = false;
		hasAssignExpr = false;
		symbol = new Symbol();
	}

	public VarDecNode(String id){
		this(new AstNode(NodeType.ID, new Token(id, TokenType.IDENTIFIER)));
	}
	
	public VarDecNode(AstNode id){
		this();
		hasVar = true;
		super.addChild(id);
		symbol.setName(id.getToken().text());
	}
	
	public void setVar(AstNode id){
		if(hasVar){
			children.remove(0);
		}
		
		if(id != null){
			this.addChildFirst(id);
			symbol.setName(id.getToken().text());
			hasVar = true;
		}else{
			hasVar = false;
		}
	}
	
	public void setAssignExpr(AstNode expr){
		if(hasAssignExpr){
			children.remove(children.size() - 1);
		}
		
		if(expr != null){
			this.addChild(expr);
			hasAssignExpr = true;
		}else{
			hasAssignExpr = false;
		}
	}
	
	public AstNode getIDNode(){
		return hasVar ? children.get(0) : null;
	}
	
	public AstNode getAssignExpr(){
		return hasAssignExpr ? children.get(1) : null;
	}
	
	public String getVarName(){
		return hasVar ? getIDNode().getToken().text() : null;
	}

}

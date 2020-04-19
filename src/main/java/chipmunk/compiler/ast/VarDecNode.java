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

import chipmunk.compiler.Symbol;

public class VarDecNode extends AstNode implements SymbolNode {
	
	protected boolean hasVar;
	protected boolean hasAssignExpr;
	protected Symbol symbol;
	
	
	public VarDecNode(){
		super();
		hasVar = false;
		hasAssignExpr = false;
		symbol = new Symbol();
	}
	
	public VarDecNode(IdNode id){
		this();
		hasVar = true;
		super.addChild(id);
		symbol.setName(id.getID().getText());
	}
	
	public void setVar(IdNode id){
		if(hasVar){
			children.remove(0);
		}
		
		if(id != null){
			this.addChildFirst(id);
			symbol.setName(id.getID().getText());
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
	
	public IdNode getIDNode(){
		return hasVar ? (IdNode) children.get(0) : null;
	}
	
	public AstNode getAssignExpr(){
		return hasAssignExpr ? children.get(1) : null;
	}
	
	public String getVarName(){
		return hasVar ? getIDNode().getID().getText() : null;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("(vardec ");
		
		if(hasVar){
			builder.append(getIDNode().getID().getText());
			
			if(hasAssignExpr){
				builder.append(' ');
			}
		}
		
		if(hasAssignExpr){
			builder.append(getAssignExpr().toString());
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public Symbol getSymbol() {
		return symbol;
	}

}

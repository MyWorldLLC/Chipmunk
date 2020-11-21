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
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.VarDecNode;

public class VarDecVisitor implements AstVisitor {

	protected Codegen codegen;
	
	public VarDecVisitor(Codegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		VarDecNode dec = (VarDecNode) node;
		
		ChipmunkAssembler assembler = codegen.getAssembler();
		Symbol symbol = codegen.getActiveSymbols().getSymbol(dec.getVarName());
		
		if(dec.getAssignExpr() != null){
			dec.getAssignExpr().visit(new ExpressionVisitor(codegen));
		}else{
			assembler.pushNull();
		}
		if(symbol == null) {
			throw new NullPointerException("Null symbol: " + dec.getVarName() + "\n" + codegen.getActiveSymbols().toString());
		}
		codegen.emitLocalAssignment(symbol.getName());
		codegen.getAssembler().pop();
	}

}

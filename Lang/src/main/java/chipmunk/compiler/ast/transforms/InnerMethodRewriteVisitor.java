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

package chipmunk.compiler.ast.transforms;

import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.SymbolTable;

/**
 * Re-writes nested method declarations (either named methods or lambda methods),
 * hoisting them to the nearest class or module scope with a mangled name. This
 * must run *after* the symbol table has been built, as it uses the symbol table
 * to find the proper parent to insert the method declaration.
 *
 * Named methods are re-written at declaration site to an AST equivalent to
 * "var namedMethod = self::mangledMethodName", lambda methods are rewritten
 * to an expression of the form "self::mangledMethodName".
 */
public class InnerMethodRewriteVisitor implements AstVisitor {

	@Override
	public void visit(AstNode node) {

		// Hoist inner methods to closest non-method scoped node
		if(node.is(NodeType.METHOD)){
			var parent = node.getSymbolTable().getParent();
			if(parent != null && parent.isMethodScope()){
				var id = Methods.getName(node);

				var hoist = hoistTo(node);
				var parentNode = node.getParent();
				var index = parentNode.indexOf(node);
				parentNode.removeChild(node);

				var nodeSymbols = node.getSymbolTable();

				// TODO - rather than leaving the symbol here, where it isn't
				// we should have a mechanism in the symbol table to reflect hoists
				//parent.removeSymbol(node.getSymbol());
				nodeSymbols.setParent(hoist.getSymbolTable());
				hoist.getSymbolTable().setSymbol(node.getSymbol());

				hoist.addChild(node);

				var rewrite = Operators.make("::", TokenType.DOUBLECOLON, node.getLineNumber(), Identifier.make("self"), id);

				if(nodeSymbols.countUpvalueRefs() > 0){
					var upvalueRefs = nodeSymbols.getUpvalueRefs();
					rewrite = Methods.makeInvocation(rewrite, "bindArgs", node.getLineNumber(),
							Literals.makeInt(Methods.getParamCount(node) - nodeSymbols.countUpvalueRefs()),
							Lists.makeListOf(upvalueRefs
									.stream()
									.map(s -> Identifier.make(s.getName(), node.getLineNumber()))
									.toArray(AstNode[]::new))
							);
					upvalueRefs.forEach(s -> {
						var param = Identifier.make(s.getName(), node.getLineNumber());
						param.setSymbol(s);
						Methods.addParam(node, param);
						nodeSymbols.setSymbol(s.makeUpvalueRef());
					});
				}
				parentNode.addChild(index, rewrite);
			}

		}

		node.visitChildren(this);
	}

	protected AstNode hoistTo(AstNode node){
		return node.getSymbolTable()
				.findTable(t -> t.getScope() == SymbolTable.Scope.CLASS || t.getScope() == SymbolTable.Scope.MODULE)
				.getNode();
	}

}

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
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.lexer.Token;
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

		if(node instanceof MethodNode methodNode){
			var parent = methodNode.getSymbolTable().getParent();
			if(parent != null && parent.isMethodScope()){
				// TODO - determine if this is a lambda or statement-style rewrite
				// & perform rewrite
			}

		}

		node.visitChildren(this);
	}

}

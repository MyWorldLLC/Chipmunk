/*
 * Copyright (C) 2026 MyWorld, LLC
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

public class VarInitRewriteVisitor implements AstVisitor {

    @Override
    public void visit(AstNode node) {
        node.visitChildren(this);
        if(node.is(NodeType.VAR_DEC) && node.getParent().is(NodeType.MODULE, NodeType.CLASS)) {
            if(node.getToken().text().startsWith("$")){
                return; // Skip import var dec fields, since those are already handled by the initializer builder
            }

            var assignExpression = VarDec.getAssignment(node);

            // Rewrite empty assign expression to null assignment
            if(assignExpression == null){
                assignExpression = Literals.make("null");
            }

            VarDec.removeAssignment(node);

            var parent = node.getParent();

            var id = Identifier.make(VarDec.getIdentifier(node).getToken());
            id.setResultType(node.getResultType());

            var dotAccess = Operators.make(".", Identifier.make("self"), id);
            dotAccess.setResultType(node.getResultType());

            var assignStatement = Operators.make("=", dotAccess, assignExpression);
            assignStatement.setResultType(node.getResultType());

            if (parent.is(NodeType.MODULE)) {
                var initializer = parent.getChild(n -> Methods.isMethodNamed(n, "$module_init$"));
                Methods.addToBody(initializer, assignStatement);
            } else if (parent.is(NodeType.CLASS)) {
                if (node.getSymbol().isShared()) {
                    Methods.addToBody(parent.getChild(0), assignStatement);
                } else {
                    Methods.addToBody(parent.getChild(1), assignStatement);
                }
            }
        }
    }
}

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
        if(node.is(NodeType.VAR_DEC)){

            AstNode assignExpression = VarDec.getAssignment(node);

            // Rewrite empty assign expression to null assignment
            if(assignExpression == null){
                assignExpression = Literals.make("null");
            }

            VarDec.removeAssignment(node);

            var owner = getOwner(node);

            var id = Identifier.make(VarDec.getIdentifier(node).getToken());

            var assignStatement = Operators.make("=", id, assignExpression);

            if (owner.is(NodeType.MODULE)) {
                var initializer = owner.getChild(n -> Methods.isMethodNamed(n, "$module_init$"));
                // Sort assignments to '$' fields (imported modules) to the front of the initializer,
                // so that the writes happen before any potential reads of those fields in the initializer.
                if(VarDec.getVarName(node).startsWith("$")){
                    Methods.addToBody(initializer,0, assignStatement);
                }else{
                    Methods.addToBody(initializer, assignStatement);
                }
            } else if (owner.is(NodeType.CLASS)) {
                if (node.getSymbol().isShared()) {
                    Methods.addToBody(owner.getChild(0), assignStatement);
                } else {
                    Methods.addToBody(owner.getChild(1), assignStatement);
                }
            }
        }
    }

    private AstNode getOwner(AstNode node) {
        var owner = node.getParent();
        while(owner != null && !owner.is(NodeType.MODULE, NodeType.CLASS)) {
            owner = owner.getParent();
        }
        return owner;
    }
}

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

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.types.BuiltinTypes;

/**
 * Notes on type resolution:
 *  1. Expressions are fairly easy: recurse to leaves, and on way back up the tree find operations either
 *      (a) defined on the leaf types or (b) defined on possible promotions of the leaf types. Mark the resolved operation,
 *      and continue unwinding recursion.
 *  2. Non-method statement blocks have no typing.
 *  3. Lambda methods are a little more subtle:
 *     (a) If the lambda is just assigned to a variable, refer to its type constraints (if any).
 *     (b) If the lambda is passed at the site of declaration as an argument to another method, compare its type constraints
 *         against the constraints of the parameter declaration it's passed for. Note that the lambda's constraints always
 *         win against the declaration site's constraints, so it is a type error if a lambda has an incompatible constraint.
 *  4. Classes are easy, because the full type is given at declaration site. Once generics are in play, the abstract type
 *     is always given at the declaration site, and the fully resolvable type is given at the construction site.
 */
public class TypeInferenceVisitor implements AstVisitor {
    @Override
    public void visit(AstNode node) {
        node.visitChildren(this); // TODO - this might be too aggressive, maybe we should selectively recurse based on what we find
        switch (node.getNodeType()){
            case ID -> {
                // TODO - get type of this symbol and set it on this node
                var symbol = node.getSymbolTable().getSymbol(node.getToken().text());
                node.setResultType(BuiltinTypes.ANY);
            }
            case OPERATOR -> {
                // TODO - check for built-in operations first. If the operator's types do not resolve
                // to a built-in op then overloaded operators will be used, so use overload method signatures to resolve
                // the type.
            }
            case METHOD -> {
                // TODO - check return types for inference or against declared type constraints
            }
        }
    }
}

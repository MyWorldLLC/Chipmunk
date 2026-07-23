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
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolType;
import chipmunk.compiler.types.*;

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
 *
 *  Note on methods: cyclical dependencies in methods can cause problems for inference when neither method can be fully resolved
 *  without also fully resolving another method in the cycle. When cycles occur, produce a warning and mark the method as returning 'Any'.
 */
public class TypeInferenceVisitor implements AstVisitor {
    @Override
    public void visit(AstNode node) {
        node.visitChildren(this); // TODO - this might be too aggressive, maybe we should selectively recurse based on what we find
        switch (node.getNodeType()){
            // Note: Literal types are set by the LiteralParselet.
            case ID -> {
                var symbol = node.getSymbolTable().getSymbol(node.getToken().text());
                node.setResultType(symbol.getReferentType()); // TODO - what to do if the symbol's type hasn't been resolved yet?
            }
            case OPERATOR -> {
                // Check for built-in operations first. If the operator's types do not resolve
                // to a built-in op then overloaded operators defined on the first operand will be used, so use
                // overload method signatures to resolve the type.
                var operandTypes = node.getChildren().stream()
                        .map(AstNode::getResultType)
                        .toArray(ObjectType[]::new);
                var operator = BuiltinOps.getOperation(node.getToken().text(), operandTypes);
                if(operator.isEmpty()){
                    // TODO - check LHS for a method with a matching signature
                }

                if(operator.isPresent()){
                    node.setResultType(operator.get().rValue());
                }else{
                    // TODO - emit warning
                    node.setResultType(AnyType.INSTANCE);
                }
            }
            case METHOD -> {
                // TODO - check return types for inference or against declared type constraints
            }
        }
    }

    protected Symbol findMethod(AstNode searchFrom, String name, ObjectType... args){
        var symTab = searchFrom.getSymbolTable();
        while(symTab != null){
            for(var symbol : symTab.getAllSymbols()){
                if(symbol.getType() == SymbolType.METHOD && symbol.getName().equals(name)){
                    var methodType = (MethodType) symbol.getReferentType();
                    if(methodType != null && methodType.argsMatch(args)){
                        // TODO - if the method doesn't have typing info yet, recurse to infer its types.
                        // Recursive typing has 2 ambiguous cases - self-recursion and mutual recursion between different
                        // methods. Self recursion can be resolved by inspecting the return types of any return statements
                        // in the method, giving the possible type bounds for that method provided that the method does
                        // not call any methods that cannot be unambiguously resolved. Cyclical recursion can be solved for
                        // the same way - as long as type ambiguity involves only the mutually recursive calls, then typing
                        // can be inferred from the resolvable return statements.
                        return symbol;
                    }
                }
            }
            symTab = symTab.getParent();
        }
        return null;
    }
}

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

import chipmunk.compiler.CompilerUtil;
import chipmunk.compiler.Intrinsics;
import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;
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
        if(!node.is(NodeType.IMPORT)){
            node.visitChildren(this); // TODO - this might be too aggressive, maybe we should selectively recurse based on what we find
        }
        switch (node.getNodeType()){
            // Note: Literal types are set by the LiteralParselet.
            case ID -> {
                var symbol = findScope(node).getSymbol(node.getToken().text());
                node.setResultType(symbol.getReferentType());
                // TODO - what to do if the symbol's type hasn't been resolved yet?
                // We can handle these cases with a multi-pass system: first pass infers everything it can, and enqueues
                // nodes that can't be resolved for later. On each pass through the queue, we infer anything we can, and re-enqueue
                // what we can't. When we can't infer the type, (for example, circular dependency), we should be able to detect
                // the cycle by inspecting the queue and either error or infer type Any.
            }
            case VAR_DEC -> {
                if(VarDec.hasAssignment(node)){
                    var type = node.getRight().getResultType();
                    // For lambda methods
                    if(node.getRight().is(NodeType.METHOD)){
                        type = new MethodType(type);
                    }
                    node.getLeft().setResultType(type);
                    // Note that the symbol table will refer to the var dec node, not its nested id, so we
                    // need to set the result type on the var dec itself as well.
                    node.setResultType(type);
                }
            }
            case OPERATOR -> {
                // Check for built-in operations first. If the operator's types do not resolve
                // to a built-in op then overloaded operators defined on the first operand will be used, so use
                // overload method signatures to resolve the type.
                var operandTypes = node.getChildren().stream()
                        .map(AstNode::getResultType)
                        .map(t -> t != null ? t : BuiltinTypes.ANY)
                        .toArray(ObjectType[]::new);
                var operator = Intrinsics.getEmitter(node.getToken().text(), operandTypes);
                if(operator.isEmpty()){
                    // TODO - check LHS for a method with a matching signature
                }

                var resolvedType = operator.map(op -> op.op().rValue())
                        .orElseGet(() -> {
                            // TODO - emit warning
                            return BuiltinTypes.ANY;
                        });

                node.setResultType(resolvedType);

                if(operator.isPresent()){
                    node.setResultType(operator.get().op().rValue());
                }else{
                    // TODO - emit warning
                    node.setResultType(AnyType.INSTANCE);
                }
            }
            case FLOW_CONTROL -> {
                if(node.getToken().type() == TokenType.RETURN){
                    node.setResultType(node.getChild(0).getResultType());
                    var method = node.getParent();
                    while(!method.is(NodeType.METHOD)){
                        method = method.getParent();
                    }
                    if(!method.alreadyHasResultType()){
                        method.setResultType(node.getResultType());
                    }else{
                        // TODO - verify compatibility
                    }
                }
            }
            case METHOD -> {
                // TODO - check return types for inference or against declared type constraints
                /*if(Methods.isLambda(node)){
                    var rType = node.getResultType(); // Type returned by this method
                    node.setResultType(new MethodType(rType));
                }*/
            }
            case LIST -> node.setResultType(BuiltinTypes.LIST);
            case MAP -> node.setResultType(BuiltinTypes.MAP);
        }
        // If we can't infer it, set to Any
        if(node.getResultType() == null){
            node.setResultType(BuiltinTypes.ANY);
        }
    }

    protected SymbolTable findScope(AstNode node){
        var table = node.getSymbolTable();
        while(table == null && node.hasParent()){
            node = node.getParent();
            table = node.getSymbolTable();
        }
        return table;
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

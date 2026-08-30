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

package chipmunk.compiler.ir.blocks;

import chipmunk.compiler.ir.LocalBlockNode;
import chipmunk.compiler.ir.ParentNode;
import chipmunk.compiler.ir.VarDecNode;
import chipmunk.compiler.ir.flow.ReturnNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.ir.passes.TypeResolutionContext;
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.MethodType;

public class MethodNode extends LocalBlockNode {

    protected final String name;
    protected final MethodType methodType;

    public MethodNode(String name, ParentNode parent, MethodType methodType) {
        super(parent);
        this.name = name;
        inferredType(methodType);
        this.methodType = methodType;
    }

    public MethodNode(String name, LocalBlockNode parent, MethodType methodType) {
        super(parent);
        this.name = name;
        inferredType(methodType);
        this.methodType = methodType;
    }

    public String name(){
        return name;
    }

    public MethodType methodType() {
        return methodType;
    }

    @Override
    public void resolveTypes(EvaluationEnvironment env, TypeResolutionContext ctx){
        super.resolveTypes(env, ctx);

        // TODO - mark upvalues by finding any descendents that refer to variables in an outer local scope

        // TODO - handle unresolved types
        var returnTypes = findDescendants(node -> node instanceof ReturnNode)
                .map(n -> (ReturnNode) n)
                .map(ReturnNode::inferredType)
                //.map(n -> n.children().isEmpty() ? BuiltinTypes.ANY : n.inferredType())
                .distinct()
                .toList();

        methodType.replaceRType(returnTypes.size() != 1 ? BuiltinTypes.ANY : returnTypes.getFirst());
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        for(var child : children){
            switch (child){
                case VarDecNode n -> ctx.evaluateVarDec(n);
                // TODO
                case ReturnNode n -> {
                    n.evaluate(env, ctx);
                    ctx.codeEvaluator()._return(n.inferredType());
                }
                default -> {}
            }
        }
    }
}

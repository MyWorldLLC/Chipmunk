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

import chipmunk.compiler.Variable;
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

    /**
     * Constructor for standard methods
     */
    public MethodNode(String name, ParentNode parent, MethodType methodType) {
        super(parent);
        this.name = name;
        inferredType(methodType);
        this.methodType = methodType;
        variables().declare(new Variable("self", this));
    }

    /**
     * Constructor for lambda methods
     */
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
        if(variables().has("self")){
            var self = variables().get("self");
            self.type(parent.inferredType());
            self.declaredType(parent.declaredType());
        }
        super.resolveTypes(env, ctx);

        // TODO - mark upvalues by finding any descendents that refer to variables in an outer local scope

        // TODO - handle unresolved types
        var returnTypes = findDescendants(node -> node instanceof ReturnNode)
                .map(n -> (ReturnNode) n)
                .map(ReturnNode::inferredType)
                .distinct()
                .toList();

        methodType.replaceRType(returnTypes.size() != 1 ? BuiltinTypes.VOID : returnTypes.getFirst());
    }

    @Override
    public void evaluateBlock(EvaluationEnvironment env, EvaluationContext ctx){
        if(children.isEmpty()){
            ctx.codeEvaluator()._return(BuiltinTypes.VOID);
        }else{
            for(var child : children){
                child.evaluate(env, ctx);
            }
        }
    }
}

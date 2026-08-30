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

package chipmunk.compiler.ir.expression;

import chipmunk.compiler.ir.ParentNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.ir.passes.TypeResolutionContext;

public class LocalGetNode extends ExpressionNode {

    protected final String name;

    public LocalGetNode(String name, ParentNode parent) {
        super(parent);
        this.name = name;
    }

    @Override
    public void resolveTypes(EvaluationEnvironment env, TypeResolutionContext ctx){
        lookupVariable(name).ifPresent(variable -> inferredType(variable.type()));
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        /*var scope = lookupVariableScope(name).get();
        var index = scope.variables().indexOf(name);
        ctx.codeEvaluator().getLocal(index, inferredType());*/
        System.out.println("Loading local " + name + " with inferred type " + inferredType());
        ctx.loadLocal(this, name, inferredType());
    }

    @Override
    public String toString(){
        return toString("");
    }

    public String toString(String indent){
        return "[" + getClass().getSimpleName() + " Inferred Type: " + inferredType + " Declared Type: " + declaredType + " Identifier: " + name + "]";
    }

}

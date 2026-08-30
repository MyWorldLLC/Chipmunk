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

import chipmunk.compiler.Intrinsics;
import chipmunk.compiler.ir.IRNode;
import chipmunk.compiler.ir.ParentNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.ir.passes.TypeResolutionContext;
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.ObjectType;
import chipmunk.compiler.types.Operation;

/**
 * Note that this is not called 'OperatorNode' because the IR builder will translate
 * multi-node syntaxes (such as a.b(), a[b] = c, etc) into their logical operations (callAt, setAt, etc).
 * It is the logical operation that is represented by this node. This saves having to search for these specific patterns in
 * the IR tree.
 */
public class OperationNode extends ExpressionNode {

    protected final String operationName;

    public OperationNode(String operationName) {
        this.operationName = operationName;
    }

    public OperationNode(String operationName, ParentNode parent){
        super(parent);
        this.operationName = operationName;
    }

    public String operationName() {
        return operationName;
    }

    @Override
    public void resolveTypes(EvaluationEnvironment env, TypeResolutionContext ctx){
        super.resolveTypes(env, ctx);
        // TODO - if not intrinsically defined, fall back to searching for a method definition that will do this operation
        var maybeOp = Intrinsics.getOperation(operationName, childTypes());
        inferredType(maybeOp.map(Operation::rValue).orElse(BuiltinTypes.ANY));
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        for(var child : children){
            child.evaluate(env, ctx);
        }
        ctx.codeEvaluator().operation(operationName, childTypes());
    }

    @Override
    public String toString(){
        return toString("");
    }

    public String toString(String indent){
        return indent + "[" + getClass().getSimpleName() + "(" + operationName + ") " + "Inferred Type: " + inferredType + " Declared Type: " + declaredType + "]";
    }

}

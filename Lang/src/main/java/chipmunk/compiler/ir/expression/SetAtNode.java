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
import chipmunk.compiler.ir.ParentNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.types.BuiltinTypes;

import java.util.List;

public class SetAtNode extends OperationNode {

    protected final AssignmentType assignmentType;

    public SetAtNode(ParentNode parent, AssignmentType assignmentType) {
        super(Intrinsics.SET_AT, parent);
        this.assignmentType = assignmentType;
    }

    public AssignmentType assignmentType(){
        return assignmentType;
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        var code = ctx.codeEvaluator();

        var target = children.get(0);
        var index = children.get(1);
        var value = children.get(2);

        var types = childTypes();
        var op = code.getOp(Intrinsics.SET_AT, types);
        if(op.isEmpty()){
            // This will be a dynamic call, so no need to do anything special with the values
            target.evaluate(env, ctx);
            index.evaluate(env, ctx);
            value.evaluate(env, ctx);
        }else{
            // This will be a statically linked call - check and convert types as needed for each parameter.
            types = ctx.evaluateAndConvertAll(List.of(target, index, value), List.of(op.get().op().pValues()));
        }

        var rType = code.operation(Intrinsics.SET_AT, types);
        if(assignmentType == AssignmentType.ASSIGN_RETURN && rType.isAssignableTo(BuiltinTypes.VOID)){
            code.pushNull();
        }else if(assignmentType == AssignmentType.ASSIGN){
            code.pop();
        }
    }
}

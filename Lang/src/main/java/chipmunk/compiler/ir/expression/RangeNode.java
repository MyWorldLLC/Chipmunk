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
import chipmunk.compiler.types.BuiltinTypes;

public class RangeNode extends OperationNode {

    protected final boolean inclusive;

    public RangeNode(ParentNode parent, boolean inclusive) {
        super("range", parent);
        this.inclusive = inclusive;
    }

    public boolean inclusive() {
        return inclusive;
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        for(var child : children){
            child.evaluate(env, ctx);
            ctx.checkAndConvert(child.inferredType(), BuiltinTypes.ANY);
        }
        var code = ctx.codeEvaluator();
        code.push(inclusive);
        code.invokeDynamic("range", BuiltinTypes.ANY, BuiltinTypes.ANY, BuiltinTypes.BOOLEAN);
    }

}

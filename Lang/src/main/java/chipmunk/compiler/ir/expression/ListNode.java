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

import java.util.ArrayList;
import java.util.List;

public class ListNode extends ExpressionNode {

    public ListNode(ParentNode parent) {
        super(parent);
        inferredType(BuiltinTypes.LIST);
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        var code = ctx.codeEvaluator();
        code.newInstance(BuiltinTypes.LIST, ArrayList.class);

        for(var child : children){
            code.dup();
            child.evaluate(env, ctx);
            ctx.checkAndConvert(child.inferredType(), BuiltinTypes.ANY);
            code.invokeInterface(BuiltinTypes.BOOLEAN, List.class, "add", BuiltinTypes.ANY);
            code.pop(); // Pop the boolean result of add()
        }
    }

}

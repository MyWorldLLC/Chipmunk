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

public class BindingNode extends OperationNode {

    protected final String name;

    public BindingNode(String name, ParentNode parent) {
        super("bind", parent);
        this.name = name;
        inferredType(BuiltinTypes.ANY);
    }

    public String name(){
        return name;
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        children.getFirst().evaluate(env, ctx);
        var code = ctx.codeEvaluator();
        code.push(name);
        code.invokeRuntime("bind", BuiltinTypes.BINDING, BuiltinTypes.ANY, BuiltinTypes.STRING);
    }
}

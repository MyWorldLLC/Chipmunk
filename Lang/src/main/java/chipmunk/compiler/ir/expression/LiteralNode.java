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
import chipmunk.compiler.types.ObjectType;

public class LiteralNode extends ExpressionNode {

    private final Object value;

    public LiteralNode(Object value, ObjectType type, ParentNode parent) {
        super(parent);
        this.value = value;
        inferredType(type);
        declaredType(type);
    }

    public Object value(){
        return value;
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        var code = ctx.codeEvaluator();
        switch (value){
            case null -> code.pushNull();
            case Boolean b -> code.push(b);
            case Byte b -> code.push(b);
            case Short s -> code.push(s);
            case Integer i  -> code.push(i);
            case Float f -> code.push(f);
            case Double d -> code.push(d);
            case String s -> code.push(s);
            default -> {}
        }
    }

    @Override
    public String toString(){
        return toString("");
    }

    public String toString(String indent){
        return indent + "[" + getClass().getSimpleName() + "Inferred Type: " + inferredType + " Declared Type: " + declaredType + " Value: " + value + "]";
    }
}

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

import chipmunk.compiler.ir.expression.ExpressionNode;
import chipmunk.compiler.ir.IRNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.types.BuiltinTypes;

import java.util.List;

public class WhileNode extends LocalBlockNode {

    public WhileNode(LocalBlockNode parent) {
        super(parent);
        inferredType(BuiltinTypes.VOID);
        declaredType(BuiltinTypes.VOID);
    }

    public ExpressionNode condition(){
        return (ExpressionNode) children.getFirst();
    }

    public List<IRNode> body(){
        return children.stream().skip(1).toList();
    }

    @Override
    public void checkSemantics(EvaluationEnvironment env){
        if(children.isEmpty()){
            env.error(this, "While loop cannot be empty. This is a compiler bug.");
            return;
        }

        if(!(children.getFirst() instanceof ExpressionNode)){
            env.error(this, "While loop guard must be an expression node. This is a compiler bug.");
        }
    }

    @Override
    public void evaluateBlock(EvaluationEnvironment env, EvaluationContext ctx){
        var code = ctx.codeEvaluator();
        code.makeLoop(block -> {
            ctx.makeBranch(condition(), block.breakLabel());
            for(var node : body()){
                node.evaluate(env, ctx);
            }
            code._goto(block.startLabel());
        });
    }

}

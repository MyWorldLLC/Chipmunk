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
import chipmunk.compiler.ir.IRNode;
import chipmunk.compiler.ir.expression.ExpressionNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.ObjectType;

import java.util.Iterator;
import java.util.List;

public class ForNode extends LocalBlockNode {

    protected final String iteratedName;

    public ForNode(String iteratedName, LocalBlockNode parent) {
        super(parent);
        this.iteratedName = iteratedName;
    }

    public String iteratedName() {
        return iteratedName;
    }

    public ExpressionNode iterator(){
        return (ExpressionNode) children.getFirst();
    }

    public List<IRNode> body(){
        return children.stream().skip(1).toList();
    }

    @Override
    public void markBlockSymbols(EvaluationEnvironment env){
        var iterated = new Variable(iteratedName, this);
        iterated.type(BuiltinTypes.ANY);
        variables().declare(iterated);

        var iterator = new Variable(iteratorName(), this);
        iterator.type(BuiltinTypes.ITERATOR);
        variables().declare(iterator);
    }

    @Override
    public void evaluateBlock(EvaluationEnvironment env, EvaluationContext ctx){
        var code = ctx.codeEvaluator();
        iterator().evaluate(env, ctx);
        code.invokeInterface(BuiltinTypes.ITERATOR, Iterable.class, "iterator");
        ctx.storeLocal(this, iteratorName(), BuiltinTypes.ITERATOR);

        code.makeLoop(block -> {
            ctx.loadLocal(this, iteratorName(), BuiltinTypes.ITERATOR);
            code.invokeInterface(BuiltinTypes.BOOLEAN, Iterator.class, "hasNext")
                 .ifeq(block.endLabel());

            ctx.loadLocal(this, iteratorName(), BuiltinTypes.ITERATOR);
            code.invokeInterface(BuiltinTypes.ANY, Iterator.class, "next");
            ctx.storeLocal(this, iteratedName, BuiltinTypes.ANY);
            for(var node : body()){
                node.evaluate(env, ctx);
            }
            code._goto(block.startLabel());
        });
    }

    protected String iteratorName(){
        return iteratedName + "$it";
    }

}

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

package chipmunk.compiler.ir;

import chipmunk.compiler.Variable;
import chipmunk.compiler.ir.expression.ExpressionNode;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.ir.passes.TypeResolutionContext;

public class VarDecNode extends ParentNode {

    protected final String name;

    public VarDecNode(String name, ParentNode parent) {
        super(parent);
        this.name = name;
    }

    public String name(){
        return name;
    }

    @Override
    public boolean isAllowedChild(IRNode c){
        return c instanceof ExpressionNode;
    }

    @Override
    public void markSymbols(EvaluationEnvironment env){
        nearestAncestor(VariableScope.class)
                .ifPresentOrElse(scope -> scope.variables().declare(new Variable(name, this)),
                        () -> {
                            throw new IllegalStateException("This variable declaration is not nested within a variable scope. This is a compiler bug.");
                        });
    }

    @Override
    public void resolveTypes(EvaluationEnvironment env, TypeResolutionContext ctx){
        super.resolveTypes(env, ctx);
        lookupVariable(name).ifPresent(variable -> variable.type(children.getFirst().inferredType()));
    }

    @Override
    public void checkSemantics(EvaluationEnvironment env){
        if(children().size() > 1){
            env.error(this, "Malformed variable declaraction for %s, cannot have more than one child", name);
        }

        var maybeVariable = lookupVariable(name);
        if(maybeVariable.isEmpty()){
            env.error(this, "Variable %s cannot be found from within its declaration scope", name);
        } else {
            var variable = maybeVariable.get();

            if(!(variable.declarationSite() instanceof VariableScope)){
                env.error(this, "Variable %s was not declared within a variable scope node: %s", name, variable.declarationSite().getClass().getName());
            }

            if(variable.type() == null){
                env.error(this, "Variable %s does not have a type assigned", name);
            }
        }
    }

}

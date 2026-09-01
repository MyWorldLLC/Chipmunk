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
import chipmunk.compiler.ir.ParentNode;
import chipmunk.compiler.ir.expression.LocalGetNode;
import chipmunk.compiler.ir.expression.LocalSetNode;
import chipmunk.compiler.ir.flow.ReturnNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.ir.passes.TypeResolutionContext;
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.MethodType;
import chipmunk.compiler.types.VoidType;

public class MethodNode extends LocalBlockNode {

    protected final String name;
    protected final MethodType methodType;

    /**
     * Constructor for standard methods
     */
    public MethodNode(String name, ParentNode parent, MethodType methodType) {
        super(parent);
        this.name = name;
        inferredType(methodType);
        this.methodType = methodType;

        if(!isLambda()){
            var self = new Variable("self", this);
            self.setFlag(Variable.FINAL);
            variables().declare(self);
        }
    }

    public String name(){
        return name;
    }

    public MethodType methodType() {
        return methodType;
    }

    @Override
    public void resolveTypes(EvaluationEnvironment env, TypeResolutionContext ctx){
        if(variables().has("self")){
            var self = variables().get("self");
            self.type(parent.inferredType());
            self.declaredType(parent.declaredType());
        }
        super.resolveTypes(env, ctx);

        // TODO - mark upvalues by finding any descendents that refer to variables in an outer local scope
        /*var outerLocalReads = findDescendants(node -> node instanceof LocalGetNode)
                .map(n -> (LocalGetNode) n)
                //.map()
                .toList();*/

        // TODO - handle unresolved types
        var returnTypes = findDescendants(node -> node instanceof ReturnNode)
                .map(n -> (ReturnNode) n)
                .map(ReturnNode::inferredType)
                .distinct()
                .toList();

        var rType = switch (returnTypes.size()){
            case 0 -> children.size() == 1 ? BuiltinTypes.ANY : BuiltinTypes.VOID;
            case 1 -> returnTypes.getFirst();
            default -> BuiltinTypes.ANY;
        };

        methodType.replaceRType(rType);
    }

    public boolean isLambda(){
        // TODO - this might not quite be enough for the case of methods within class expressions
        return !(parent instanceof ClassNode || parent instanceof ModuleNode);
    }

    @Override
    public void evaluateBlock(EvaluationEnvironment env, EvaluationContext ctx){
        if(isLambda() && !ctx.isEvaluatingLambdas()){
            ctx.enqueueLambda(this);

            // The lambda method implementation will be hoisted into the nearest class, so we can
            // just emit a binding for self::<this method>
            var code = ctx.codeEvaluator();
            ctx.loadLocal(this, "self", BuiltinTypes.ANY);
            // TODO - bind closure params
            code.push(name);
            code.invokeRuntime("bind", BuiltinTypes.BINDING, BuiltinTypes.ANY, BuiltinTypes.STRING);

        }else if(!isLambda() || ctx.isEvaluatingLambdas()){
            for(var child : children){
                child.evaluate(env, ctx);
            }

            // Generate default return
            switch (methodType.rType()){
                case VoidType _ -> ctx.codeEvaluator()._return(BuiltinTypes.VOID);
                default -> {
                    ctx.pushZeroValue(methodType.rType());
                    ctx.codeEvaluator()._return(methodType.rType());
                }
            }
        }else{
            /*// The lambda method implementation will be hoisted into the nearest class, so we can
            // just emit a binding for self::<this method>
            var code = ctx.codeEvaluator();
            ctx.loadLocal(this, "self", BuiltinTypes.ANY);
            code.push(name);
            code.invokeRuntime("bind", BuiltinTypes.BINDING, BuiltinTypes.ANY, BuiltinTypes.STRING);*/
        }
    }
}

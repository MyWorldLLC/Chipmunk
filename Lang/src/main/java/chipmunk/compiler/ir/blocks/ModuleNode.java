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

import chipmunk.compiler.SymbolStorage;
import chipmunk.compiler.Variable;
import chipmunk.compiler.ir.*;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.ir.passes.TypeResolutionContext;
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.MethodType;
import chipmunk.compiler.types.ModuleType;

import java.util.Optional;

public class ModuleNode extends ParentNode implements VariableScope {

    public static final String INITIALIZER_NAME = "$module_init$";

    protected final ModuleType moduleType;
    protected String fileName;

    public ModuleNode(ModuleType moduleType) {
        this.moduleType = moduleType;
        fileName = "<unknown>";
    }

    public ModuleType moduleType() {
        return moduleType;
    }

    public void fileName(String fileName) {
        if(fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName cannot be null or blank");
        }
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }

    @Override
    public boolean isAllowedChild(IRNode c){
        return c instanceof VarDecNode
                || c instanceof ClassNode
                || c instanceof MethodNode
                || c instanceof DocNode;
    }

    @Override
    public Optional<Variable> lookupVariable(String name){
        if(moduleType.variables().has(name)){
            return Optional.of(moduleType.variables().get(name));
        }
        return Optional.empty();
    }

    @Override
    public Optional<VariableScope> lookupVariableScope(String name){
        if(moduleType.variables().has(name)){
            return Optional.of(this);
        }
        return Optional.empty();
    }

    @Override
    public void resolveTypes(EvaluationEnvironment env, TypeResolutionContext ctx){
        inferredType(moduleType);
        super.resolveTypes(env, ctx);
    }

    @Override
    public void markSymbols(EvaluationEnvironment env){
        var variables = variables();
        moduleType.imports().stream()
                .forEach(imp -> {
                    var name = "$" + imp.module();
                    if(!variables.has(name)) {
                        var variable = new Variable(name, this, imp);
                        variable.type(BuiltinTypes.ANY);
                        variable.setFlag(Variable.FINAL);
                        variables.declare(variable);
                    }

                    if(variables.has(imp.name())){
                        env.error(this, imp.name() + " is already imported in module " + moduleType.name());
                    }else{
                        var variable = new Variable(imp.name(), this, imp);
                        variable.setFlag(Variable.FINAL);
                        variable.type(BuiltinTypes.ANY);
                        variables.declare(variable);
                    }
                });

        // TODO - variables, classes, & methods
    }

    // TODO - resolve types

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        // Evaluation consists of emitting classes, emitting methods, and emitting the initializer
        for(var child : children){
            switch (child){
                //case VarDecNode n -> n.evaluate(env, ctx);
                case ClassNode n -> ctx.evaluateClass(n);
                case MethodNode n -> ctx.evaluateMethod(n);
                default -> {} // This should never be hit because we've already validated the IR
            }
        }

       // super.generateInitializers(env);
        //var init = new MethodNode(INITIALIZER_NAME, this, new MethodType(BuiltinTypes.VOID));
        // Initializer emit goes in the following order:
        // 1. Init imports in import order
        // 2. Init class fields
        // 3. Run class shared initializers & init variables in declaration order. This allows
        //    class shared variables & module variables to be initialized in an expected order.
        // TODO

        ctx.writeSyntheticMethod(INITIALIZER_NAME, new MethodType(BuiltinTypes.VOID), code -> {

            for(var imp : moduleType.imports()){
                // TODO
            }

            for(var child : children){
                switch (child){
                    // We evaluate the var decs here so that they have a code builder to use for initialization
                    case VarDecNode n -> n.evaluate(env, ctx);
                    case ClassNode n -> {
                        // TODO - do module-level initialization of class
                    }
                    default -> {}
                }
            }
            code._return(BuiltinTypes.VOID);
        });

    }

    @Override
    public SymbolStorage<Variable> variables() {
        return moduleType.variables();
    }
}

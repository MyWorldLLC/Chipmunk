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
import chipmunk.compiler.ir.flow.ReturnNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
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
        return super.lookupVariable(name);
    }

    @Override
    public void generateInitializers(EvaluationEnvironment env){
        super.generateInitializers(env);
        var init = new MethodNode(INITIALIZER_NAME, this, new MethodType(BuiltinTypes.VOID));
        // TODO
        init.addChild(new ReturnNode(init));
        addChild(init);
    }

    @Override
    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){
        // Evaluation consists of emitting classes, emitting methods, and emitting the initializer

        for(var child : children){
            switch (child){
                case VarDecNode n -> ctx.evaluateVarDec(n);
                case ClassNode n -> ctx.evaluateClass(n);
                case MethodNode n -> ctx.evaluateMethod(n);
                default -> {} // This should never be hit because we've already validated the IR
            }
        }

        // Initializer emit goes in the following order:
        // 1. Init imports in import order
        // 2. Init class fields
        // 3. Run class shared initializers & init variables in declaration order. This allows
        //    class shared variables & module variables to be initialized in an expected order.
    }

    @Override
    public SymbolStorage<Variable> variables() {
        return moduleType.variables();
    }
}

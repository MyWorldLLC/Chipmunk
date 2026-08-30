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

package chipmunk.compiler.ir.passes;

import chipmunk.compiler.CodeEvaluator;
import chipmunk.compiler.Variable;
import chipmunk.compiler.ir.VarDecNode;
import chipmunk.compiler.ir.blocks.ClassNode;
import chipmunk.compiler.ir.blocks.MethodNode;
import chipmunk.compiler.ir.blocks.ModuleNode;

import java.util.Optional;

public interface EvaluationContext {

    Optional<Variable> lookupVariable(String varName);
    void evaluateModule(ModuleNode module);
    void evaluateClass(ClassNode classNode);
    void evaluateMethod(MethodNode method);
    void evaluateVarDec(VarDecNode varDec);
    CodeEvaluator codeEvaluator();

}

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

import chipmunk.compiler.ir.blocks.ModuleNode;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.types.ObjectType;

import java.util.Objects;

public abstract class IRNode {

    public static final int DEBUG_NONE = -1;

    protected final IRNode parent;

    protected ObjectType inferredType;
    protected ObjectType declaredType;

    protected int line;
    protected int column;

    public IRNode(){
        this(null);
    }

    public IRNode(IRNode parent) {
        this.parent = parent;
        line = DEBUG_NONE;
        column = DEBUG_NONE;
    }

    public void debugInfo(int line, int column){
        this.line = line;
        this.column = column;
    }

    public int lineNumber(){
        return line;
    }

    public int columnNumber(){
        return column;
    }

    public IRNode parent() {
        return parent;
    }

    public void inferredType(ObjectType inferredType){
        Objects.requireNonNull(inferredType);
        this.inferredType = inferredType;
    }

    public ObjectType inferredType(){
        return inferredType;
    }

    public void declaredType(ObjectType constraintType){
        Objects.requireNonNull(constraintType);
        if(this.declaredType != null){
            throw new IllegalStateException("Constraint type already set");
        }
        this.declaredType = constraintType;
    }

    public ObjectType declaredType(){
        return declaredType;
    }

    public boolean hasDeclaredType(){
        return declaredType != null;
    }

    public boolean hasInferredType(){
        return inferredType != null;
    }

    public void markSymbols(EvaluationEnvironment env){}

    public void resolveTypes(EvaluationEnvironment env){}

    public void checkSemantics(EvaluationEnvironment env){
        if(!hasInferredType()){
            env.error(this, "Node does not have an inferred type. This is a compiler bug.");
            return;
        }
        if(hasDeclaredType()){
            if(!inferredType.isAssignableTo(declaredType) && !inferredType.canPromoteTo(declaredType)){
                env.error(this, "Type error: %s cannot be assigned or promoted to %s", inferredType, declaredType);
            }
        }
    }

    public void evaluate(EvaluationEnvironment env){}

    public ModuleNode getModule(){
        var candidate = this;
        while(candidate != null){
            if(candidate instanceof ModuleNode){
                return (ModuleNode) candidate;
            }
            candidate = candidate.parent();
        }
        throw new IllegalStateException("This node is not attached to a module");
    }
}

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
import chipmunk.compiler.ir.blocks.ClassNode;
import chipmunk.compiler.ir.blocks.MethodNode;
import chipmunk.compiler.ir.blocks.ModuleNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.ir.passes.TypeResolutionContext;
import chipmunk.compiler.types.ObjectType;
import chipmunk.compiler.types.UnresolvedType;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class IRNode {

    public static final int DEBUG_NONE = -1;

    protected final ParentNode parent;

    protected ObjectType inferredType;
    protected ObjectType declaredType;

    protected int line;
    protected int column;

    public IRNode(){
        this(null);
    }

    public IRNode(ParentNode parent) {
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

    public ParentNode parent() {
        return parent;
    }

    public boolean hasParent(){
        return parent != null;
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

    public void resolveTypes(EvaluationEnvironment env, TypeResolutionContext ctx){}

    protected void handleUnresolvedType(EvaluationEnvironment env, TypeResolutionContext ctx, IRNode node, ObjectType type){
        if(type instanceof UnresolvedType){
            ctx.enqueueTask(() -> node.resolveTypes(env, ctx));
        }else{
            node.inferredType(type);
        }

    }

    public void checkSemantics(EvaluationEnvironment env){
        if(!hasInferredType()){
            env.error(this, "Node does not have an inferred type. This is a compiler bug.");
            return;
        }
        if(hasDeclaredType()){
            if(!env.typeConflict(inferredType, declaredType)){
                env.error(this, "Type error: %s cannot be assigned or promoted to %s", inferredType, declaredType);
            }
        }
    }

    public void evaluate(EvaluationEnvironment env, EvaluationContext ctx){}

    public Optional<MethodNode> containingMethodNode(){
        return nearestAncestor(MethodNode.class);
    }

    public Optional<ClassNode> containingClassNode(){
        return nearestAncestor(ClassNode.class);
    }

    public Optional<ModuleNode> containingModuleNode(){
        return nearestAncestor(ModuleNode.class);
    }

    public Optional<Variable> lookupVariable(String name){
        if(hasParent()){
            return parent().lookupVariable(name);
        }
        return Optional.empty();
    }

    public Optional<VariableScope> lookupVariableScope(String name){
        if(hasParent()){
            return parent().lookupVariableScope(name);
        }
        return Optional.empty();
    }

    protected <T> Optional<T> nearestAncestor(Class<T> type){
        var node = this;
        while(node.hasParent()){
            node = node.parent();
            if(type.isInstance(node)){
                return Optional.of(type.cast(node));
            }
        }
        return Optional.empty();
    }

    public Stream<IRNode> ancestors(){
        return Stream.iterate(this, IRNode::hasParent, IRNode::parent);
    }

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

    @Override
    public String toString(){
        return toString("");
    }

    public String toString(String indent){
        return "[" + getClass().getSimpleName() + " Inferred Type: " + inferredType + " Declared Type: " + declaredType + "]";
    }
}

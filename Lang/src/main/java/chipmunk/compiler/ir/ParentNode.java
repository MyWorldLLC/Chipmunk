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

import chipmunk.compiler.ir.passes.EvaluationEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ParentNode extends IRNode {

    protected final List<IRNode> children;

    public ParentNode(){
        this(null);
    }

    public ParentNode(ParentNode parent){
        super(parent);
        children = new ArrayList<>();
    }

    public void addChild(IRNode child){
        if(child.parent() != this){
            throw new IllegalArgumentException("Child is not parented to this node");
        }
        if(!isAllowedChild(child)){
            throw new IllegalArgumentException("Child type " + child.getClass().getName() + " is not a valid child of " + getClass().getName());
        }
        children.add(child);
    }

    public boolean isAllowedChild(IRNode child){
        return true;
    }

    public List<IRNode> children() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void markSymbols(EvaluationEnvironment env){
        for(var child : children){
            child.markSymbols(env);
        }
    }

    @Override
    public void resolveTypes(EvaluationEnvironment env){
        for(var child : children){
            child.resolveTypes(env);
        }
    }

    @Override
    public void checkSemantics(EvaluationEnvironment env){
        for(var child : children){
            child.checkSemantics(env);
        }
    }

    @Override
    public void evaluate(EvaluationEnvironment env){
        for(var child : children){
            child.evaluate(env);
        }
    }

}

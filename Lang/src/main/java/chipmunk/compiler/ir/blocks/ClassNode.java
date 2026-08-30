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
import chipmunk.compiler.types.ClassType;

import java.util.List;
import java.util.Optional;

public class ClassNode extends ParentNode implements VariableScope {

    protected final ClassType classType;
    protected final List<ClassType> traits;

    public ClassNode(ClassType classType, ParentNode parent) {
        this(classType, parent, List.of());
    }

    public ClassNode(ClassType classType, ParentNode parent, List<ClassType> traits) {
        super(parent);
        this.classType = classType;
        this.traits = List.copyOf(traits);
    }

    public ClassType classType() {
        return classType;
    }

    public List<ClassType> traits() {
        return traits;
    }

    public boolean hasTraits(){
        return !traits.isEmpty();
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
        if(classType.variables().has(name)){
            return Optional.of(classType.variables().get(name));
        }
        return super.lookupVariable(name);
    }

    @Override
    public SymbolStorage<Variable> variables() {
        return classType.variables();
    }

}

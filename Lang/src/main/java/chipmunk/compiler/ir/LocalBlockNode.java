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

import chipmunk.compiler.SymbolStorage;
import chipmunk.compiler.Variable;
import chipmunk.compiler.types.BuiltinTypes;

import java.util.Optional;

public abstract class LocalBlockNode extends ParentNode implements VariableScope {

    protected final SymbolStorage<Variable> locals;

    /**
     * Use this constructor for the method root block
     * @param parent
     */
    public LocalBlockNode(ParentNode parent) {
        super(parent);
        this.locals = new SymbolStorage<>();
        inferredType(BuiltinTypes.VOID);
        declaredType(BuiltinTypes.VOID);
    }

    /**
     * Use this constructor for nested blocks within a method
     * @param parent parent node
     */
    public LocalBlockNode(LocalBlockNode parent) {
        super(parent);
        this.locals = new SymbolStorage<>(parent.variables());
    }

    @Override
    public SymbolStorage<Variable> variables() {
        return locals;
    }

    @Override
    public Optional<Variable> lookupVariable(String name){
        if(locals.has(name)){
            return Optional.of(locals.get(name));
        }
        return super.lookupVariable(name);
    }
}

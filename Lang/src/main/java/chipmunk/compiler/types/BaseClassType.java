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

package chipmunk.compiler.types;

import chipmunk.compiler.MethodDef;
import chipmunk.compiler.SymbolStorage;
import chipmunk.compiler.Variable;

public abstract class BaseClassType extends ObjectType {

    protected final SymbolStorage<Variable> variables;
    protected final SymbolStorage<MethodDef> methods;
    protected final SymbolStorage<ClassType> classes;

    public BaseClassType(String name) {
        super(name);

        variables = new SymbolStorage<>();
        methods = new SymbolStorage<>();
        classes = new SymbolStorage<>();
    }

    public SymbolStorage<Variable> variables(){
        return variables;
    }

    public SymbolStorage<MethodDef> methods(){
        return methods;
    }

    public SymbolStorage<ClassType> classes(){
        return classes;
    }
}

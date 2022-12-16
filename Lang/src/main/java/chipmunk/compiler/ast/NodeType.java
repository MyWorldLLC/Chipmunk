/*
 * Copyright (C) 2022 MyWorld, LLC
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

package chipmunk.compiler.ast;

import chipmunk.compiler.symbols.SymbolTable;

public enum NodeType {
    MODULE(SymbolTable.Scope.MODULE),
    IMPORT(),
    CLASS(SymbolTable.Scope.CLASS),
    METHOD(SymbolTable.Scope.METHOD),
    VAR_DEC(),
    ID(),
    IF_ELSE(),
    IF(SymbolTable.Scope.LOCAL),
    ELSE(SymbolTable.Scope.LOCAL),
    FLOW_CONTROL(),
    KEY_VALUE(),
    ITERATOR(),
    LIST(),
    LITERAL(),
    MAP(),
    MATCH(SymbolTable.Scope.LOCAL),
    CASE(SymbolTable.Scope.LOCAL),
    WHEN(SymbolTable.Scope.LOCAL),
    OPERATOR(),
    TRY_CATCH(),
    TRY(SymbolTable.Scope.LOCAL),
    CATCH(SymbolTable.Scope.LOCAL),
    FINALLY(SymbolTable.Scope.LOCAL),
    WHILE(SymbolTable.Scope.LOCAL),
    FOR(SymbolTable.Scope.LOCAL);

    private final SymbolTable.Scope scope;

    NodeType(){
        this(null);
    }

    NodeType(SymbolTable.Scope scope){
        this.scope = scope;
    }

    public boolean isBlock(){
        return scope != null;
    }

    public SymbolTable.Scope getScope(){
        return scope;
    }

    public String getDebugName(){
        return name();
    }
}

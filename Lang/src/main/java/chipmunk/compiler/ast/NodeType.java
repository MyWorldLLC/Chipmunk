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

public enum NodeType {
    MODULE(true),
    IMPORT(false),
    CLASS(true),
    METHOD(true),
    VAR_DEC(false),
    ID(false),
    IF_ELSE(true),
    ITERATOR(false),
    LIST(false),
    LITERAL(false),
    MAP(false),
    MATCH(true),
    CASE(true),
    WHEN(true),
    OPERATOR(false),
    TRY(true),
    CATCH(true),
    FINALLY(true),
    WHILE(true),
    FOR(true);

    private final boolean block;

    NodeType(boolean block){
        this.block = block;
    }

    public boolean isBlock(){
        return block;
    }

}

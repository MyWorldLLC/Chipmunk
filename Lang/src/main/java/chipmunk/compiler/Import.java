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

package chipmunk.compiler;

import chipmunk.compiler.types.ObjectType;

public class Import extends Typed<ObjectType> {

    protected final String module;
    protected final String aliasOf;

    public Import(String name, String module) {
        super(name);
        this.module = module;
        this.aliasOf = null;
    }

    public Import(String name, String module, String aliasOf) {
        super(name);
        this.module = module;
        this.aliasOf = aliasOf;
    }

    public boolean isAliased(){
        return aliasOf != null;
    }

    public String aliasOf(){
        return aliasOf;
    }

    public String module(){
        return module;
    }
}

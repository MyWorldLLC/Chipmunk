/*
 * Copyright (C) 2025 MyWorld, LLC
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

package chipmunk.runtime;


import java.util.Objects;

public class CMethod {

    public CModule module;
    public String name;
    public int argCount;
    public byte[] code;
    public int localCount;

    // We use a stored hash so that this is fast when computing fiber caches
    private final int hash;

    public CMethod(CModule module, String name, int argCount){
        this.module = module;
        this.name = name;
        this.argCount = argCount;
        hash = Objects.hash(module.name, name, argCount);
    }

    public CMethod(CModule module, String name, int argCount, byte[] code){
        this.module = module;
        this.name = name;
        this.argCount = argCount;
        this.code = code;
        hash = Objects.hash(module.name, name, argCount);
    }

    @Override
    public int hashCode(){
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CMethod cMethod)) return false;
        return argCount == cMethod.argCount && Objects.equals(name, cMethod.name) && Objects.equals(module, cMethod.module);
    }
}

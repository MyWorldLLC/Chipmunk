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

package chipmunk.compiler.types;

import java.util.List;

public record ObjectType(String name, boolean isPrimitive, boolean isType, List<ObjectType> superTypes) {

    public static final ObjectType ANY = new ObjectType("Any", false, true);
    public static final ObjectType INT = new ObjectType("Int", true, false);
    public static final ObjectType FLOAT = new ObjectType("Float", true, false);
    public static final ObjectType LONG = new ObjectType("Long", true, false);
    public static final ObjectType DOUBLE = new ObjectType("Double", true, false);

    public ObjectType(String name, boolean isPrimitive, boolean isType){
        this(name, isPrimitive, isType, List.of());
    }

    public ObjectType typeOf(){
        return classOf(name, superTypes);
    }

    public static ObjectType primitive(String name){
        return new ObjectType(name, true, false, List.of());
    }

    public static ObjectType classBased(String name){
        return new ObjectType(name, false, false, List.of());
    }

    public static ObjectType classOf(String name, List<ObjectType> superTypes){
        return new ObjectType(name, false, true, List.copyOf(superTypes));
    }
}

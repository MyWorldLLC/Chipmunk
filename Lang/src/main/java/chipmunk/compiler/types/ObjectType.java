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

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

public class BuiltinTypes {

    public static final ObjectType ANY = AnyType.INSTANCE;
    public static final ObjectType VOID = VoidType.INSTANCE;

    public static final ObjectType BOOLEAN = BooleanType.INSTANCE;

    public static final ObjectType BYTE = IntegerType.BYTE;
    public static final ObjectType SHORT = IntegerType.SHORT;
    public static final ObjectType INT = IntegerType.INT;
    public static final ObjectType LONG = IntegerType.LONG;

    public static final ObjectType FLOAT = FloatType.FLOAT;
    public static final ObjectType DOUBLE = FloatType.DOUBLE;

    public static final ObjectType STRING = StringType.INSTANCE;

    public static final ObjectType MAP = CollectionType.MAP;
    public static final ObjectType LIST = CollectionType.LIST;
}

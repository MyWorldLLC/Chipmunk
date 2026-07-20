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

public class IntegerType extends PrimitiveType {

    public static final IntegerType BYTE = new IntegerType("Byte", 8);
    public static final IntegerType SHORT = new IntegerType("Short", 16);
    public static final IntegerType INT = new IntegerType("Int", 32);
    public static final IntegerType LONG = new IntegerType("Long", 64);

    private IntegerType(String name, int bits) {
        super(name, bits);
    }

    @Override
    public boolean canPromoteTo(ObjectType other){
        return switch (other){
            case IntegerType i -> i.bitSize() >= bitSize();
            case BooleanType b -> true;
            case FloatType f -> true;
            case StringType s -> true;
            default -> super.canPromoteTo(other);
        };
    }
}

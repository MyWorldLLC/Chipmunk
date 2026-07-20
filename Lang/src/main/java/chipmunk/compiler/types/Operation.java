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

public record Operation(ObjectType rValue, ObjectType... pValues) {

    public Operation{
        if(pValues == null || pValues.length == 0){
            throw new IllegalArgumentException("pValues must not be null or empty");
        }
        if(rValue == null){
            throw new IllegalArgumentException("rValue must not be null");
        }
    }

    public static Operation unary(ObjectType pType){
        return new Operation(pType, pType);
    }

    public static Operation binOp(ObjectType pType){
        return new Operation(pType, pType, pType);
    }

    public static Operation binOp(ObjectType rType, ObjectType pType){
        return new Operation(rType, pType, pType);
    }

    public static Operation binOp(ObjectType rType, ObjectType p0, ObjectType p1){
        return new Operation(rType, p0, p1);
    }

    public boolean isExactMatch(ObjectType... operands) {
        if (pValues().length != operands.length) {
            return false;
        }
        for (int i = 0; i < operands.length; i++) {
            if (!pValues()[i].equals(operands[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isPromotableMatch(ObjectType... operands) {
        if (pValues().length != operands.length) {
            return false;
        }
        for (int i = 0; i < operands.length; i++) {
            if (!operands[i].canPromoteTo(pValues()[i])) {
                return false;
            }
        }
        return true;
    }
}

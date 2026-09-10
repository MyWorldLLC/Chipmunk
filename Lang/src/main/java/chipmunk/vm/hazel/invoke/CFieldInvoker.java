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

package chipmunk.vm.hazel.invoke;

import chipmunk.runtime.CClass;
import chipmunk.runtime.CField;
import chipmunk.runtime.CModule;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.Value;

public class CFieldInvoker extends FieldInvoker {

    protected final double typePtr;
    protected final CField field;
    protected final int fieldIndex;

    public CFieldInvoker(double typePtr, CField field, int fieldIndex) {
        super(field.name());
        this.typePtr = typePtr;
        this.field = field;
        this.fieldIndex = fieldIndex;
    }

    @Override
    public boolean canInvoke(Object target) {
        // TODO - should specialize for each case
        return switch (target){
            case double[] ins -> ins.length > 0 && Value.pointersEqual(ins[0], typePtr);
            case CModule module -> Value.pointersEqual(module.selfPtr(), typePtr);
            case CClass cls -> Value.pointersEqual(cls.selfPtr(), typePtr);
            default -> false;
        };
    }

    @Override
    public double invokeGet(Fiber fiber, int bp, int sp) {
        var target = fiber.stack[bp + sp - 1];
        return switch (fiber.vm().heap().read(target)){
            case double[] ins -> ins[fieldIndex];
            case CModule module -> module.getFields()[fieldIndex];
            case CClass cls -> cls.sharedFields()[fieldIndex];
            default -> Value.NULL_PTR_VALUE; // Because of the check above, this shouldn't be possible in practice
        };
    }

    @Override
    public double invokeSet(Fiber fiber, int bp, int sp) {
        var target = fiber.stack[bp + sp - 2];
        var value = fiber.stack[bp + sp - 1];
        if(fieldIndex == 0 && fiber.vm().heap().read(target) instanceof double[]){
            // TODO - remove this and replace with proper support for 'final'.
            throw new IllegalArgumentException("Cannot set field 0 for instance");
        }
        switch (fiber.vm().heap().read(target)){
            case double[] ins -> ins[fieldIndex] = value;
            case CModule module -> module.getFields()[fieldIndex] = value;
            case CClass cls -> cls.sharedFields()[fieldIndex] = value;
            default -> {} // Because of the check above, this shouldn't be possible in practice
        }
        return value;
    }
}

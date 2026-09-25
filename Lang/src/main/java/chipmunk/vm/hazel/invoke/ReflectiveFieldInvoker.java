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

import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.TypeError;
import chipmunk.vm.hazel.Value;

import java.lang.reflect.Field;

public class ReflectiveFieldInvoker extends FieldInvoker {

    protected final Class<?> cls;
    protected final Field field;

    public ReflectiveFieldInvoker(Class<?> cls, Field field, String name) {
        super(name);
        this.cls = cls;
        this.field = field;
    }

    @Override
    public boolean canInvoke(Object target) {
        return cls.isInstance(target);
    }

    @Override
    public double invokeGet(Fiber fiber, int bp, int sp, Object target) {
        try{
            return fiber.vm().fromHostValue(field.get(target));
        } catch (Throwable t) {
            throw new TypeError(fiber, target.getClass().getName() + "." + name + ") is not gettable: " + t.getMessage(), t);
        }
    }

    @Override
    public double invokeSet(Fiber fiber, int bp, int sp, Object target) {
        var heap = fiber.vm().heap();

        var stackValue = fiber.stack[bp + sp - 1];

        Object value = null; // Assume null pointer because that's free.
        if(Value.isNumber(stackValue)){
            value = stackValue;
        }else if(Value.isPointer(stackValue)){
            value = heap.read(stackValue);
        }

        try{
            field.set(target, value);
            return stackValue;
        } catch (Throwable t) {
            throw new TypeError(fiber, target.getClass().getName() + "." + name + ") is not gettable: " + t.getMessage(), t);
        }
    }

}

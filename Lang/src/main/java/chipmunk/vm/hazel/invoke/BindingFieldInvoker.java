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
import chipmunk.vm.hazel.Value;
import chipmunk.vm.hazel.invoke.binding.FieldModel;

public class BindingFieldInvoker<O> extends FieldInvoker {

    protected final Class<?> cls;
    protected final FieldModel<O, ?> model;

    public BindingFieldInvoker(String name, Class<O> cls, FieldModel<O, ?> model) {
        super(name);
        this.cls = cls;
        this.model = model;
    }
    @Override
    public boolean canInvoke(Object target) {
        return cls.isInstance(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public double invokeGet(Fiber fiber, int bp, int sp, Object target) {
        var heap = fiber.vm().heap();
        return fiber.vm().fromHostValue(model.getter().apply((O) target));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public double invokeSet(Fiber fiber, int bp, int sp, Object target) {
        var heap = fiber.vm().heap();
        var stackValue = fiber.stack[bp + sp - 1];

        var value = Value.isNumber(stackValue) ? stackValue : !Value.isNullPointer(stackValue) ? heap.read(stackValue) : null;
        ((FieldModel) model).setter().accept(target, value);
        return stackValue;
    }
}

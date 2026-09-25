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
import chipmunk.vm.hazel.invoke.binding.MethodModel;

public class BindingMethodInvoker<O> extends MethodInvoker {

    protected final Class<O> type;
    protected final MethodModel<O> model;

    public BindingMethodInvoker(String name, Class<O> type, MethodModel<O> model) {
        super(name, model.paramTypes().length + 1);
        this.model = model;
        this.type = type;
    }

    @Override
    public boolean canInvoke(Object target) {
        return type.isInstance(target);
    }

    @Override
    public int invokeMethod(Fiber fiber, int ip, int bp, int sp, Object t) {
        var heap = fiber.vm().heap();
        var target = type.cast(t);
        var pTypes = model.paramTypes();
        var params = new Object[pTypes.length];
        for(int i = 0; i < params.length; i++) {
            var p = fiber.readArg(bp, sp, argCount, i + 1); // Have to offset by 1 since arg 0 is always the target
            if(Value.isNumber(p)){
                params[i] = p;
            }else{
                params[i] = heap.read(p);
            }
        }
        var result = model.invoker().apply(target, params);
        fiber.pushResult(bp, sp, argCount, fiber.vm().fromHostValue(result));
        return ip + 1;
    }
}

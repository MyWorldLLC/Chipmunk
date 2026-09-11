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

import chipmunk.runtime.HostCObject;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.TypeError;
import chipmunk.vm.hazel.Value;

import java.lang.reflect.Method;

public class ReflectiveMethodInvoker extends MethodInvoker {

    private static final Object[] EMPTY_ARGS = new Object[0];

    protected final Class<?> cls;
    protected final Method method;

    public ReflectiveMethodInvoker(Class<?> cls, Method method, String name, int argCount) {
        super(name, argCount);
        this.cls = cls;
        this.method = method;
    }

    @Override
    public boolean canInvoke(Object target) {
        return cls.isInstance(target);
    }

    @Override
    public int invokeMethod(Fiber fiber, int ip, int bp, int sp, Object target) {
        var heap = fiber.vm().heap();

        // TODO - probably not necessary since we already have a native reference to the object
        var targetPtr = fiber.stack[bp + sp - argCount];
        if(!Value.isPointer(targetPtr)) {
            throw new TypeError(fiber, "Not a reference to an object");
        }

        // We have to adjust parameter count due to the fact that Chipmunk includes the 'self' parameter in the count,
        // while Java does not.
        var pCount = argCount - 1;
        var params = EMPTY_ARGS;
        if(pCount > 0){
            params = new Object[pCount];
            for(int i = 0; i < pCount; i++) {
                var p = fiber.stack[bp + sp - argCount + i + 1];
                if(Value.isPointer(p)){
                    params[i] = heap.read(p);
                }else{
                    params[i] = p;
                }
            }
        }
        try {
            var result = method.invoke(target, params);
            fiber.stack[bp + sp - argCount] = switch (result){
                case Double d -> d;
                case null -> Value.NULL_PTR_VALUE;
                case HostCObject cObj -> {
                    if(!Value.isNullPointer(cObj.selfPtr())){
                        yield cObj.selfPtr();
                    }
                    var ptr = heap.allocateAndWrite(cObj);
                    cObj.selfPtr(ptr);
                    yield ptr;
                }
                default -> heap.allocateAndWrite(result);
            };
            return ip + 1;
        } catch (Throwable t) {
            throw new TypeError(fiber, target.getClass().getName() + "." + name + "(" + (argCount - 1) + ") is not callable: " + t.getMessage(), t);
        }
    }
}

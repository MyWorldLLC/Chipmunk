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

package chipmunk.vm.hazel.instructions;

import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.Instruction;
import chipmunk.vm.hazel.Value;
import chipmunk.vm.hazel.invoke.Invoker;
import chipmunk.vm.hazel.invoke.MethodInvoker;

public abstract class CallingInstruction extends Instruction {

    protected final Invoker invoker;
    protected MethodInvoker method;

    protected CallingInstruction(int sp, Invoker invoker) {
        super(sp);
        this.invoker = invoker;
    }

    protected MethodInvoker getMethodInvoker(double targetPtr, Object target, Fiber fiber, String name, int args) {
        if(method != null && method.canInvoke(target)){
            return method;
        }
        method = invoker.methodInvokerFor(fiber, targetPtr, name, args);
        return method;
    }

    public int dynamicCall(Fiber fiber, int ip, int bp, int sp, String methodName, int args){
        var ptr = fiber.stack[bp + sp - args];
        var heap = fiber.vm().heap();
        var obj = heap.read(Value.getPointer(ptr));
        return getMethodInvoker(ptr, obj, fiber, methodName, args).invokeMethod(fiber, ip, bp, sp);
    }
}

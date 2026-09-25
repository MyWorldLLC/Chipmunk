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

import chipmunk.runtime.CObject;
import chipmunk.runtime.TraitGuard;
import chipmunk.vm.hazel.Fiber;

import java.util.ArrayDeque;
import java.util.Deque;

public class TraitMethodInvoker extends MethodInvoker {

    protected final double selfPtr;
    protected final MethodInvoker invoker;
    protected final Deque<TraitGuard> guards;

    public TraitMethodInvoker(double selfPtr, MethodInvoker invoker) {
        super(invoker.name, invoker.argCount);
        this.selfPtr = selfPtr;
        this.guards = new ArrayDeque<>(5);
        this.invoker = invoker;
    }

    public void addToChain(TraitGuard guard){
        guards.add(guard);
    }

    @Override
    public boolean canInvoke(Object target) {
        if(target instanceof CObject obj && obj.selfPtr() == selfPtr){
            for(TraitGuard guard : guards){
                if(guard.isTripped()){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int invokeMethod(Fiber fiber, int ip, int bp, int sp, Object target) {
        var obj = (CObject) target;
        for(TraitGuard guard : guards){
            obj = (CObject) fiber.vm().heap().read(obj.getField(guard.guardedField()));
        }
        fiber.stack[bp + sp - argCount] = obj.selfPtr(); // Replace the self pointer
        return invoker.invokeMethod(fiber, ip, bp, sp, obj);
    }
}

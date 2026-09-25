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

public class TraitFieldInvoker extends FieldInvoker{

    protected final double selfPtr;
    protected final FieldInvoker invoker;
    protected final Deque<TraitGuard> guards;

    public TraitFieldInvoker(double selfPtr, FieldInvoker invoker) {
        super(invoker.name());
        this.selfPtr = selfPtr;
        this.invoker = invoker;
        this.guards = new ArrayDeque<>(5);
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
    public double invokeGet(Fiber fiber, int bp, int sp, Object target) {
        var obj = (CObject) target;
        for(TraitGuard guard : guards){
            obj = (CObject) fiber.vm().heap().read(obj.getField(guard.guardedField()));
        }
        return invoker.invokeGet(fiber, bp, sp, obj);
    }

    @Override
    public double invokeSet(Fiber fiber, int bp, int sp, Object target) {
        var obj = (CObject) target;
        for(TraitGuard guard : guards){
            obj = (CObject) fiber.vm().heap().read(obj.getField(guard.guardedField()));
        }
        return invoker.invokeSet(fiber, bp, sp, obj);
    }
}

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
import chipmunk.runtime.CMethod;
import chipmunk.runtime.CModule;
import chipmunk.runtime.CObject;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.Value;

public class CMethodInvoker extends MethodInvoker {

    protected final double typePtr;
    protected final CMethod method;

    public CMethodInvoker(double typePtr, CMethod method) {
        super(method.name(), method.argCount());
        this.typePtr = typePtr;
        this.method = method;
    }

    @Override
    public boolean canInvoke(Object target) {
        // TODO - should specialize for each case
        return switch (target){
            case CObject ins -> Value.pointersEqual(ins.storage()[0], typePtr);
            case CModule module -> Value.pointersEqual(module.selfPtr(), typePtr);
            case CClass cls -> Value.pointersEqual(cls.selfPtr(), typePtr);
            default -> false;
        };
    }

    @Override
    public int invokeMethod(Fiber fiber, int ip, int bp, int sp, Object target) {
        return fiber.vm().invokeMethod(method, fiber, ip, bp, sp);
    }
}

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

public class NativeMethodInvoker extends MethodInvoker {

    protected final Class<?> type;
    protected final NativeMethod method;

    public NativeMethodInvoker(String name, Class<?> type, NativeMethod method, int argCount) {
        super(name, argCount);
        this.type = type;
        this.method = method;
    }

    @Override
    public boolean canInvoke(Object target) {
        return type.isInstance(target);
    }

    @Override
    public int invokeMethod(Fiber fiber, int ip, int bp, int sp, Object target) {
        return method.invoke(fiber, ip, bp, sp, argCount, target);
    }
}

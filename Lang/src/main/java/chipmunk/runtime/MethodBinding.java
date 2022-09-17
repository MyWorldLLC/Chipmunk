/*
 * Copyright (C) 2022 MyWorld, LLC
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

package chipmunk.runtime;

import chipmunk.vm.ChipmunkVM;
import chipmunk.vm.invoke.Binder;
import chipmunk.vm.invoke.ChipmunkLinker;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.support.SimpleLinkRequest;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public class MethodBinding {

    protected final Object target;
    protected final String methodName;
    protected final ChipmunkLinker linker;
    protected volatile GuardedInvocation handle;

    public MethodBinding(Object target, String methodName){
        this.target = target;
        this.methodName = methodName;
        linker = new ChipmunkLinker();
    }

    public Object call(Object... args) throws Throwable {
        if(handle == null || handle.hasBeenInvalidated()){
            handle = linker.getInvocationHandle(MethodHandles.lookup(), target, null, methodName, args);
        }

        Objects.requireNonNull(handle, "Could not bind %s::%s(%s)".formatted(target, methodName, args));

        return handle.getInvocation().invoke(args);
    }
}

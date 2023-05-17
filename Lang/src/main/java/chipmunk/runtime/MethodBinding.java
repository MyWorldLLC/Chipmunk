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

import chipmunk.vm.invoke.ChipmunkLinker;
import jdk.dynalink.linker.GuardedInvocation;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public abstract class MethodBinding {

    public static final String TARGET_FIELD_NAME = "target";

    protected final Object target;
    protected final String methodName;
    protected final ChipmunkLinker linker;
    protected final AtomicReference<GuardedInvocation> handle;

    public MethodBinding(Object target, String methodName){
        this.target = target;
        this.methodName = methodName;
        handle = new AtomicReference<>();
        linker = new ChipmunkLinker();
    }

    public Object getTarget(){
        return target;
    }

    public String getMethodName(){
        return methodName;
    }

}

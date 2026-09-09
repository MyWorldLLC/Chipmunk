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
import chipmunk.runtime.CModule;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.TypeError;
import chipmunk.vm.hazel.Value;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;

public class Invoker {

    protected final LinkingPolicy linkingPolicy;

    public Invoker() {
        this(new LinkingPolicy(SecurityMode.ALLOWING));
    }

    public Invoker(LinkingPolicy linkingPolicy) {
        this.linkingPolicy = linkingPolicy;
    }

    public MethodInvoker methodInvokerFor(Fiber fiber, double ptr, String name, int args){
        var heap = fiber.vm().heap();
        if(Value.isNullPointer(ptr)){
            throw new TypeError(fiber, "Cannot call null." + name + "(" + args + ")");
        }
        var target = heap.read(ptr);
        if(target == null){
            throw new TypeError(fiber, "Cannot call null." + name + "(" + args + ")");
        }
        if(target instanceof double[] ins){
            var clsPtr = ins[0];
            var cClass = (CClass) heap.read(clsPtr);
            var method = cClass.findMethod(cClass.instanceMethodDefs(), name, args);
            if(method == null){
                throw new TypeError(fiber, "Method does not exist: " + cClass.name() + "." + name + "(" + args + ")");
            }
            return new CMethodInvoker(clsPtr, method);
        }else{
            if(target instanceof CModule m){
                var method = m.getMethod(name, args);
                if(method != null){
                    return new CMethodInvoker(m.selfPtr(), method);
                }
            }
            // TODO - check for specific type defs before falling back to reflection.
            var targetType = target.getClass();
            for(var method : targetType.getMethods()){
                // TODO - check linking policy
                if(method.getParameterCount() + 1 == args && method.getName().equals(name)){
                    method.setAccessible(true);
                    return new NativeMethodInvoker(targetType, method, name, args);
                }
            }
            throw new TypeError(fiber, "Method does not exist: " + targetType.getName() + "." + name + "(" + args + ")");
        }
    }

}

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
import chipmunk.runtime.CObject;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.TypeError;
import chipmunk.vm.hazel.Value;
import chipmunk.vm.hazel.invoke.binding.NativeBinding;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;

import java.util.Arrays;

public class Linker {

    protected final LinkingPolicy linkingPolicy;
    protected final NativeBinding binding;

    public Linker() {
        this(new LinkingPolicy(SecurityMode.ALLOWING));
    }

    public Linker(LinkingPolicy linkingPolicy) {
        this.linkingPolicy = linkingPolicy;
        this.binding = new NativeBinding();
    }

    public NativeBinding binding() {
        return binding;
    }

    // TODO - method & field invocation don't yet support traits

    public MethodInvoker linkMethod(Fiber fiber, double ptr, String name, int args){
        var heap = fiber.vm().heap();
        if(Value.isNullPointer(ptr)){
            throw new TypeError(fiber, "Cannot call null." + name + "(argCount: " + args + ")");
        }
        var target = heap.read(ptr);
        if(target == null){
            throw new TypeError(fiber, "Cannot call null." + name + "(argCount: " + args + ")");
        }

        CClass traitBase = null;
        if(target instanceof CObject ins){
            var clsPtr = ins.storage()[0];
            var cClass = (CClass) heap.read(clsPtr);
            var method = cClass.findMethod(cClass.instanceMethodDefs(), name, args);
            if(method != null){
                return new CMethodInvoker(clsPtr, method);
            }
            traitBase = cClass; // Later on, if there is no native method defined we'll search traits.
        }else if(target instanceof CModule m){
            var method = m.getMethod(name, args);
            if(method != null){
                return new CMethodInvoker(m.selfPtr(), method);
            }
        }else if(target instanceof CClass c){
            var method = c.findMethod(c.sharedMethodDefs(), name, args);
            if(method != null){
                return new CMethodInvoker(c.selfPtr(), method);
            }
        }
        var targetType = target.getClass();

        var model = binding.modelFor(target.getClass());
        if(model != null){
            var nativeMethod = model.getNativeMethod(name);
            if(nativeMethod != null){
                return new NativeMethodInvoker(name, targetType, nativeMethod, args);
            }
            var method = model.getMethod(name);
            if(method != null){
                return new BindingMethodInvoker(name, targetType, method);
            }
        }
        for(var method : targetType.getMethods()){
            if(method.getParameterCount() + 1 == args && method.getName().equals(name)){
                if(method.isAnnotationPresent(AllowChipmunkLinkage.class) || linkingPolicy.allowMethodCall(target, method)){
                    method.setAccessible(true);
                    return new ReflectiveMethodInvoker(targetType, method, name, args);
                }
            }
        }
        if(traitBase != null){
            var ins = (CObject) target;
            var fields = traitBase.instanceFieldDefs();
            for(int i = 0; i < fields.length; i++){
                var field = fields[i];
                if(field.isTrait()){
                    var traitTarget = ins.storage()[i];
                    if(!Value.isNullPointer(traitTarget)){
                        var invoker = linkMethod(fiber, traitTarget, name, args);
                        if(invoker != null){
                            // TODO - switch points & trait chain binding.
                            return invoker;
                        }
                    }
                }
            }
        }
        return null;
    }

    public MethodInvoker methodInvokerFor(Fiber fiber, double ptr, String name, int args){
        var invoker = linkMethod(fiber, ptr, name, args);
        if(invoker != null){
            return invoker;
        }
        throw new TypeError(fiber, "Method does not exist: " + fiber.vm().typeName(ptr) + "." + name + "(argCount: " + args + ")");
    }

    public FieldInvoker linkField(Fiber fiber, double ptr, String name, boolean assign){
        var heap = fiber.vm().heap();
        if(Value.isNullPointer(ptr)){
            throw new TypeError(fiber, "Cannot access null." + name);
        }
        var target = heap.read(ptr);
        if(target == null){
            throw new TypeError(fiber, "Cannot access null." + name);
        }

        CClass traitBase = null;
        if(target instanceof CObject ins){
            var clsPtr = ins.storage()[0];
            var cClass = (CClass) heap.read(clsPtr);
            var field = cClass.getField(cClass.instanceFieldDefs(), name);
            if(field >= 0){
                return new CFieldInvoker(clsPtr, cClass.instanceFieldDefs()[field], field);
                //throw new TypeError(fiber, "Field does not exist: " + cClass.name() + "." + name);
            }
            traitBase = cClass; // Later on we'll use this to search traits
        }else if (target instanceof CModule m) {
            var field = m.getField(name);
            if (field >= 0) {
                return new CFieldInvoker(m.selfPtr(), m.getFieldDefs()[field], field);
            }
        } else if (target instanceof CClass c) {
            var field = c.getField(c.sharedFieldDefs(), name);
            if (field >= 0) {
                return new CFieldInvoker(c.selfPtr(), c.sharedFieldDefs()[field], field);
            }
        }

        var targetType = target.getClass();
        var model = binding.modelFor(target.getClass());
        if (model != null) {
            var field = model.getField(name);
            if (field != null) {
                return new BindingFieldInvoker(name, targetType, field);
            }
        }
        for (var field : targetType.getFields()) {
            if (field.getName().equals(name)) {
                if (field.isAnnotationPresent(AllowChipmunkLinkage.class)
                        || (assign ? linkingPolicy.allowFieldSet(target, field) : linkingPolicy.allowFieldGet(target, field))) {
                    field.setAccessible(true);
                    return new ReflectiveFieldInvoker(targetType, field, name);
                }
            }
        }
        if(traitBase != null){
            var ins = (CObject) target;
            var fields = traitBase.instanceFieldDefs();
            for(int i = 0; i < fields.length; i++){
                var field = fields[i];
                if(field.isTrait()){
                    var traitTarget = ins.storage()[i];
                    var invoker = linkField(fiber, traitTarget, name, assign);
                    if(invoker != null){
                        // TODO - switch points & trait chain binding.
                        return invoker;
                    }
                }
            }
        }
        return null;
    }

    public FieldInvoker fieldInvokerFor(Fiber fiber, double ptr, String name, boolean assign){
        var invoker = linkField(fiber, ptr, name, assign);
        if(invoker != null){
            return invoker;
        }
        throw new TypeError(fiber, "Field does not exist: " + fiber.vm().typeName(ptr) + "." + name);
    }

}

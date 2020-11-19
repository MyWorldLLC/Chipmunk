/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk.vm.invoke;

import chipmunk.runtime.ChipmunkClass;
import chipmunk.runtime.ChipmunkObject;
import chipmunk.runtime.TraitField;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.invoke.security.LinkingPolicy;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ChipmunkLinker implements GuardingDynamicLinker {

    protected final ThreadLocal<ChipmunkLibraries> libraries;

    protected final MethodHandles.Lookup lookup;

    public ChipmunkLinker(){

        libraries = new ThreadLocal<>();
        libraries.set(new ChipmunkLibraries());

        lookup = MethodHandles.lookup();
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {

        Object receiver = linkRequest.getReceiver();
        NamedOperation op = (NamedOperation) linkRequest.getCallSiteDescriptor().getOperation();
        MethodType callType = linkRequest.getCallSiteDescriptor().getMethodType();

        Object[] params = linkRequest.getArguments();

        if(op.getBaseOperation().equals(StandardOperation.CALL)){
            // Bind method calls
            return getInvocationHandle(lookup, receiver, callType, (String)op.getName(), params);
        }else if(op.getBaseOperation().equals(StandardOperation.GET)){
            // Bind field access
            Object target = linkRequest.getReceiver();
            Objects.requireNonNull(target, "Cannot access fields on a null reference");

            Field field = getField(target, (String) op.getName());
            if(field == null){
                throw new NoSuchFieldException(target.getClass().getName() + "." + op.getName());
            }
            LinkingPolicy linkPolicy = getLinkingPolicy();
            if(linkPolicy != null && !linkPolicy.allowFieldGet(receiver, field)){
                throw new IllegalAccessException(target.getClass().getName() + "." + op.getName() + ": policy forbids get");
            }

            MethodHandle fieldHandle = lookup.unreflectVarHandle(field)
                    .toMethodHandle(VarHandle.AccessMode.GET);

            MethodHandle guard = lookup.findStatic(
                    this.getClass(),
                    "validateFieldAccess",
                    MethodType.methodType(boolean.class, Object.class, Object.class))
                    .bindTo(target);

            return new GuardedInvocation(fieldHandle, guard);

        }else if( op.getBaseOperation().equals(StandardOperation.SET)){
            // Bind field set
            Object target = linkRequest.getReceiver();
            Objects.requireNonNull(target, "Cannot access fields on a null reference");

            Field field = getField(target.getClass(), (String) op.getName());
            if(field == null){
                throw new NoSuchFieldException(target.getClass().getName() + "." + op.getName());
            }

            LinkingPolicy linkPolicy = getLinkingPolicy();
            if(linkPolicy != null && !linkPolicy.allowFieldSet(receiver, field, linkRequest.getArguments()[1])){
                throw new IllegalAccessException(target.getClass().getName() + "." + op.getName() + ": policy forbids set to " + linkRequest.getArguments()[1]);
            }

            MethodHandle fieldHandle = lookup.unreflectVarHandle(field)
                    .toMethodHandle(VarHandle.AccessMode.SET);

            // When setting a field that is a trait, we need to invalidate the current switch point for that
            // trait to invalidate any method handles that are bound to it.
            TraitField[] traits = null;
            if(target instanceof ChipmunkObject) {
                traits = ((ChipmunkObject) target).getChipmunkClass().getTraits();
            }else if(target instanceof ChipmunkClass){
                traits = ((ChipmunkClass) target).getSharedTraits();
            }

            if(traits != null){
                TraitField trait = TraitField.getField(traits, field.getName());
                if(trait != null){
                    MethodHandle invalidationHandle = lookup.findStatic(ChipmunkLinker.class, "setTraitField",
                            MethodType.methodType(void.class, TraitField.class, MethodHandle.class, Object.class, Object.class));
                    fieldHandle = MethodHandles.insertArguments(invalidationHandle, 0, trait, fieldHandle);
                }
            }

            MethodHandle guard = lookup.findStatic(
                    this.getClass(),
                    "validateFieldAccess",
                    MethodType.methodType(boolean.class, Object.class, Object.class))
                    .bindTo(target);

            return new GuardedInvocation(fieldHandle, guard);
        }

        return null;
    }

    public GuardedInvocation getInvocationHandle(MethodHandles.Lookup lookup, Object receiver, MethodType callType, String methodName, Object[] params) throws Exception {

        if(receiver == null){
            throw new NullPointerException("Invocation target is null");
        }

        Class<?>[] pTypes = new Class<?>[params.length];
        for(int i = 0; i < params.length; i++){
            pTypes[i] = params[i] != null ? params[i].getClass() : void.class;
        }

        GuardedInvocation invocation = resolveCallTarget(lookup, receiver, callType, methodName, params, pTypes);

        if(invocation == null){
            // Failed to resolve method or a trait providing the method
            throw new NoSuchMethodException(
                    formatMethodSignature(receiver.getClass(), methodName, pTypes));
        }

        return invocation;
    }

    public GuardedInvocation resolveCallTarget(MethodHandles.Lookup lookup, Object receiver, MethodType callType, String methodName, Object[] params, Class<?>[] pTypes) throws Exception {
        // Library methods should override type methods, so check them first
        Class<?> expectedReturnType = callType.returnType();
        MethodHandle callTarget = getLibraries().getMethod(lookup, expectedReturnType, methodName, pTypes);

        if (callTarget == null) {
            callTarget = getMethod(receiver, expectedReturnType, methodName, params, pTypes);
        }

        if (callTarget != null) {
            // Return non-trait invocation
            return new GuardedInvocation(callTarget, getCallGuard(lookup, callType, params));
        }

        // Check for trait methods
        TraitField[] traits = null;
        if (receiver instanceof ChipmunkObject) {
            traits = ((ChipmunkObject) receiver).getChipmunkClass().getTraits();
        } else if (receiver instanceof ChipmunkClass) {
            traits = ((ChipmunkClass) receiver).getSharedTraits();
        }

        if (traits != null) {
            for (TraitField trait : traits) {

                ReentrantLock traitLock = trait.getLock();
                try {
                    traitLock.lock();

                    Field receiverField = trait.getReflectedField();
                    if (receiverField == null) {
                        receiverField = receiver.getClass().getField(trait.getField());
                        receiverField.setAccessible(true);
                        trait.setReflectedField(receiverField);
                    }

                    Object traitReceiver = receiverField.get(receiver);
                    if (traitReceiver == null) {
                        continue;
                    }

                    GuardedInvocation invocation = resolveCallTarget(lookup, traitReceiver, callType, methodName, params, pTypes);

                    if (invocation != null) {

                        MethodHandle receiverFilter = lookup.unreflectGetter(trait.getReflectedField())
                                .asType(MethodType.methodType(traitReceiver.getClass(), pTypes[0]));

                        invocation = invocation
                                .addSwitchPoint(trait.getInvalidationPoint())
                                .replaceMethods(
                                        MethodHandles.filterArguments(invocation.getInvocation(), 0, receiverFilter),
                                        invocation.getGuard()
                                );

                        return invocation;
                    }

                } finally {
                    traitLock.unlock();
                }

            }
        }

        return null;
    }

    public MethodHandle getMethod(Object receiver, Class<?> expectedReturnType, String methodName, Object[] params, Class<?>[] pTypes) throws IllegalAccessException {

        Class<?> receiverType = receiver.getClass();

        for (Method m : receiverType.getMethods()) {
            Class<?>[] candidatePTypes = m.getParameterTypes();

            if (candidatePTypes.length != pTypes.length - 1) {
                continue;
            }

            if (!m.getName().equals(methodName)) {
                continue;
            }

            Class<?> retType = m.getReturnType();

            // TODO - check type conversions on primitive return types
            if (retType.equals(void.class) || expectedReturnType.isAssignableFrom(retType) || retType.isPrimitive()) {

                boolean paramsMatch = true;
                for (int i = 0; i < candidatePTypes.length; i++) {

                    // Need to offset by 1 because the incoming types include the receiver type
                    // while the candidate types do not
                    Class<?> callPType = pTypes[i + 1];
                    Class<?> candidatePType = candidatePTypes[i];

                    if (!candidatePType.isAssignableFrom(callPType)) {
                        paramsMatch = false;
                        break;
                    }
                }

                if (paramsMatch) {
                    // We have a match!
                    LinkingPolicy linkPolicy = getLinkingPolicy();
                    if(linkPolicy != null && !linkPolicy.allowMethodCall(receiver, m, params)){
                        throw new IllegalAccessException(formatMethodSignature(receiverType, methodName, pTypes) + ": policy forbids call");
                    }
                    m.setAccessible(true);
                    return lookup.unreflect(m);
                }

            }
        }
        return null;
    }

    public Field getField(Object receiver, String fieldName) throws Exception {
        Field[] fields = receiver.getClass().getFields();
        for(Field f : fields){
            if(f.getName().equals(fieldName)){
                return f;
            }
        }

        TraitField[] traitFields = null;
        if(receiver instanceof ChipmunkObject){
            traitFields = ((ChipmunkObject) receiver).getChipmunkClass().getTraits();
        }else if(receiver instanceof ChipmunkClass){
            traitFields = ((ChipmunkClass) receiver).getSharedTraits();
        }

        if(traitFields == null){
            return null;
        }

        for(TraitField trait : traitFields){
            ReentrantLock traitLock = trait.getLock();
            try {
                traitLock.lock();

                Field receiverField = trait.getReflectedField();
                if (receiverField == null) {
                    Class<?> receiverClass = receiver.getClass();
                    for(Field f : receiverClass.getFields()){
                        if(f.getName().equals(fieldName)){
                            receiverField = f;
                            receiverField.setAccessible(true);
                            trait.setReflectedField(receiverField);
                            break;
                        }
                    }
                }

                if(receiverField == null){
                    continue;
                }

                Object traitReceiver = receiverField.get(receiver);
                if (traitReceiver == null) {
                    continue;
                }

                Field f = getField(traitReceiver, fieldName);
                if(f != null){
                    return f;
                }

            }finally{
                traitLock.unlock();
            }
        }

        return null;
    }

    public static boolean validateCall(Object[] boundArgs, Object[] callArgs){
        if(boundArgs.length != callArgs.length){
            return false;
        }

        for(int i = 0; i < boundArgs.length; i++){
            Class<?> boundType = boundArgs[i] != null ? boundArgs[i].getClass() : null;
            Class<?> callType = callArgs[i] != null ? callArgs[i].getClass() : null;

            if(boundType != callType){
                return false;
            }
        }
        return true;
    }

    public static boolean validateFieldAccess(Object boundTarget, Object fieldValue){
        if(fieldValue == null){
            return true;
        }
        return boundTarget.getClass().isAssignableFrom(fieldValue.getClass());
    }

    public static void setTraitField(TraitField f, MethodHandle setter, Object target, Object value) throws Throwable {
        SwitchPoint.invalidateAll(new SwitchPoint[]{f.getInvalidationPoint()});
        setter.invoke(target, value);
    }

    protected MethodHandle getCallGuard(MethodHandles.Lookup lookup, MethodType callType, Object[] params) throws NoSuchMethodException, IllegalAccessException {
        MethodType guardType = callType.generic().changeReturnType(Boolean.class);

        return lookup.findStatic(
                this.getClass(),
                "validateCall",
                MethodType.methodType(boolean.class, Object[].class, Object[].class))
                .bindTo(params)
                .asCollector(Object[].class, guardType.parameterCount());
    }

    public ChipmunkLibraries getLibraries(){
        return libraries.get();
    }

    public void setLibraries(ChipmunkLibraries libraries){
        this.libraries.set(libraries);
    }

    protected LinkingPolicy getLinkingPolicy(){
        ChipmunkScript script = ChipmunkScript.getCurrentScript();
        if(script == null){
            return null;
        }

        return script.getLinkPolicy();
    }

    public String formatMethodSignature(Class<?> receiverType, String methodName, Class<?>[] pTypes){
        return receiverType.getName() + "." + methodName + "(" +
                Arrays.stream(pTypes)
                        .skip(1) // Skip self reference
                        .map(c -> c != null ? c.getName() : "null")
                        .collect(Collectors.joining(","))
                + ")";
    }

}

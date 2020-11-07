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

package chipmunk.invoke;

import jdk.dynalink.NamedOperation;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
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

            MethodHandle handle = getInvocationHandle(lookup, receiver, callType.returnType(), (String)op.getName(), params);

            MethodType guardType = callType.generic().changeReturnType(Boolean.class);

            MethodHandle guard = lookup.findStatic(
                    this.getClass(),
                    "validateCall",
                    MethodType.methodType(boolean.class, Object[].class, Object[].class))
                    .bindTo(params)
                    .asCollector(Object[].class, guardType.parameterCount());

            return new GuardedInvocation(handle, guard);
        }else if(op.getBaseOperation().equals(StandardOperation.GET)){
            // Bind field access
            Object target = linkRequest.getReceiver();
            Objects.requireNonNull(target, "Cannot access fields on a null reference");

            Field field = getField(target.getClass(), (String) op.getName());
            if(field == null){
                throw new NoSuchFieldException(target.getClass().getName() + "." + (String) op.getName());
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

            MethodHandle fieldHandle = lookup.unreflectVarHandle(field)
                    .toMethodHandle(VarHandle.AccessMode.SET);

            MethodHandle guard = lookup.findStatic(
                    this.getClass(),
                    "validateFieldAccess",
                    MethodType.methodType(boolean.class, Object.class, Object.class))
                    .bindTo(target);

            return new GuardedInvocation(fieldHandle, guard);
        }

        return null;
    }

    public MethodHandle getInvocationHandle(MethodHandles.Lookup lookup, Object receiver, Class<?> expectedReturnType, String methodName, Object[] params) throws Exception {

        if(receiver == null){
            throw new NullPointerException("Invocation target is null");
        }

        Class<?> receiverType = receiver.getClass();

        Class<?>[] pTypes = new Class<?>[params.length];
        for(int i = 0; i < params.length; i++){
            pTypes[i] = params[i] != null ? params[i].getClass() : void.class;
        }

        // Library methods should override type methods, so check them first
        MethodHandle callTarget = getLibraries().getMethod(lookup, expectedReturnType, methodName, pTypes);

        if(callTarget == null) {

            Method instanceMethod = getMethod(receiverType, expectedReturnType, methodName, pTypes);
            if(instanceMethod != null){
                instanceMethod.setAccessible(true);
                callTarget = lookup.unreflect(instanceMethod);
            }

        }

        if(callTarget == null){
            throw new NoSuchMethodException(
                    receiverType.getName() + "." + methodName + "(" + Arrays.stream(pTypes).map(c -> c != null ? c.getName() : "null").collect(Collectors.toList()) + ")");
        }

        return callTarget;
    }

    public Method getMethod(Class<?> receiverType, Class<?> expectedReturnType, String methodName, Class<?>[] pTypes){
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
                    // TODO - check security policy
                    // We have a match!
                    return m;
                }

            }
        }
        return null;
    }

    public Field getField(Class<?> receiver, String fieldName){
        Field[] fields = receiver.getFields();
        for(Field f : fields){
            if(f.getName().equals(fieldName)){
                return f;
            }
        }
        return null;
    }

    protected static boolean validateCall(Object[] boundArgs, Object[] callArgs){
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

    protected static boolean validateFieldAccess(Object boundTarget, Object fieldValue){
        if(fieldValue == null){
            return true;
        }
        return boundTarget.getClass().isAssignableFrom(fieldValue.getClass());
    }

    public ChipmunkLibraries getLibraries(){
        return libraries.get();
    }

    public void setLibraries(ChipmunkLibraries libraries){
        this.libraries.set(libraries);
    }
}

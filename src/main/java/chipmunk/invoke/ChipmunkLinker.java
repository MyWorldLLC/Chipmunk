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

import chipmunk.AngryChipmunk;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChipmunkLinker implements GuardingDynamicLinker {

    protected final ThreadLocal<ChipmunkLibraries> libraries;

    protected final MethodHandles.Lookup lookup;
    protected final MethodHandle guard;

    public ChipmunkLinker(){

        libraries = new ThreadLocal<>();
        libraries.set(new ChipmunkLibraries());

        lookup = MethodHandles.lookup();

        try {
            guard = lookup.bind(this, "validateCall", MethodType.methodType(Boolean.class, Object[].class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AngryChipmunk(e);
        }
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {

        Object receiver = linkRequest.getReceiver();
        NamedOperation op = (NamedOperation) linkRequest.getCallSiteDescriptor().getOperation();
        MethodType callType = linkRequest.getCallSiteDescriptor().getMethodType();


        Object[] params = linkRequest.getArguments();

        MethodHandle handle = getInvocationHandle(lookup, receiver, callType.returnType(), (String)op.getName(), params);

        // TODO - guard by parameter types

        return new GuardedInvocation(handle, guard);
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

            if (retType.equals(void.class) || expectedReturnType.isAssignableFrom(retType)) {

                boolean paramsMatch = true;
                for (int i = 0; i < candidatePTypes.length; i++) {

                    Class<?> callPType = pTypes[i];
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

    protected Boolean validateCall(Object[] args){
        return true;
    }

    public ChipmunkLibraries getLibraries(){
        return libraries.get();
    }

    public void setLibraries(ChipmunkLibraries libraries){
        this.libraries.set(libraries);
    }
}

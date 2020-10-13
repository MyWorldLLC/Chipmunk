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
import java.util.Collections;
import java.util.List;

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

        Class<?> receiverType = linkRequest.getReceiver().getClass();

        NamedOperation op = (NamedOperation) linkRequest.getCallSiteDescriptor().getOperation();
        MethodType callType = linkRequest.getCallSiteDescriptor().getMethodType();

        // Library methods should override type methods, so check them first

        Object[] params = linkRequest.getArguments();
        Class<?>[] pTypes = new Class<?>[params.length];
        for(int i = 0; i < params.length; i++){
            pTypes[i] = params[i] != null ? params[i].getClass() : void.class;
        }

        MethodHandle callTarget = getLibraries().getMethod(lookup, callType.returnType(), (String)op.getName(), pTypes);

        if(callTarget == null) {

            for (Method m : receiverType.getMethods()) {
                Class<?>[] candidatePTypes = m.getParameterTypes();

                if (candidatePTypes.length != pTypes.length) {
                    continue;
                }

                if (!m.getName().equals(op.getName())) {
                    continue;
                }

                Class<?> retType = m.getReturnType();

                if (retType.equals(void.class) || callType.returnType().isAssignableFrom(retType)) {

                    for (int i = 0; i < candidatePTypes.length; i++) {

                        Class<?> callPType = pTypes[i];
                        Class<?> candidatePType = candidatePTypes[i];

                        if (!candidatePType.isAssignableFrom(callPType)) {
                            break;
                        }
                    }

                    // TODO - check security policy
                    // We have a match!
                    callTarget = lookup.unreflect(m);
                    break;
                }
            }
        }

        if(callTarget == null){
            return null;
        }

        MethodHandle handle = callTarget;

        // TODO - guard by parameter types

        return new GuardedInvocation(handle, guard);
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

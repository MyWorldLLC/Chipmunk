/*
 * Copyright (C) 2025 MyWorld, LLC
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

import chipmunk.vm.invoke.security.LinkingPolicy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static chipmunk.vm.invoke.ChipmunkLinker.formatMethodSignature;
import static chipmunk.vm.invoke.ChipmunkLinker.isCallTypeCompatible;

public class CLinker {

    protected final LinkingPolicy linkingPolicy;
    protected final ChipmunkLibraries libraries;

    public CLinker(LinkingPolicy linkingPolicy, ChipmunkLibraries libraries){
        this.linkingPolicy = linkingPolicy;
        this.libraries = libraries;
    }

    public Method getMethod(Object receiver, String methodName, Object[] params, Class<?>[] pTypes, boolean enforceLinkagePolicy) throws IllegalAccessException, NoSuchMethodException{
        Class<?> receiverType;
        if(receiver == null){
            receiverType = Object.class;
        }else if(receiver instanceof Class){
            receiverType = (Class<?>) receiver;
        }else{
            receiverType = receiver.getClass();
        }
        var libMethod = libraries.getMethod(receiverType, methodName, pTypes);
        if(libMethod != null){
            return libMethod;
        }

        var methods = receiverType.getMethods();
        //linkOrder(methods);
        for (Method m : methods) {
            Class<?>[] candidatePTypes = m.getParameterTypes();

            if (candidatePTypes.length != pTypes.length && !m.isVarArgs()) {
                continue;
            }

            if (!getMethodName(m).equals(methodName)) {
                continue;
            }

            Class<?> retType = m.getReturnType();

            //if (retType.equals(void.class) || isCallTypeCompatible(expectedReturnType, retType)) {

                boolean paramsMatch = true;
                long interfaceParamMask = 0;
                boolean isStatic = Modifier.isStatic(m.getModifiers());
                for (int i = 0; i < candidatePTypes.length; i++) {

                    Class<?> callPType = pTypes[i];
                    Class<?> candidatePType = candidatePTypes[i];

                    if (!isCallTypeCompatible(candidatePType, callPType != null ? callPType : Object.class)) {
                        if (candidatePType.isInterface()) {
                            interfaceParamMask |= (1L << i);
                            continue;
                        }
                        paramsMatch = false;
                        break;
                    }
                }

                if (paramsMatch || m.isVarArgs()) {
                    // We have a match!
                    if (linkingPolicy != null && enforceLinkagePolicy && !linkingPolicy.allowMethodCall(receiver, m, params)) {
                        throw new IllegalAccessException(formatMethodSignature(receiver, methodName, pTypes) + ": policy forbids call");
                    }
                    m.setAccessible(true);
                    return m;
                }
            //}
        }
        return null;
    }

    protected String getMethodName(Method m){
        ChipmunkName override = m.getAnnotation(ChipmunkName.class);
        return override != null ? override.value() : m.getName();
    }
}

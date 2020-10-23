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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChipmunkLibraries {

    public static class LibraryMethod {
        public final Method m;
        public final ChipmunkLibrary lib;

        public LibraryMethod(Method m, ChipmunkLibrary lib){
            this.m = m;
            this.lib = lib;
        }
    }

    protected final ConcurrentHashMap<Class<?>, List<LibraryMethod>> libraries;

    public ChipmunkLibraries(){
        libraries = new ConcurrentHashMap<>();
    }

    private List<LibraryMethod> getLibraryMethodsForType(Class<?> receiverType){
        List<LibraryMethod> methods = libraries.get(receiverType);
        return methods != null ? methods : Collections.emptyList();
    }

    public MethodHandle getMethod(MethodHandles.Lookup lookup, Class<?> returnType, String name, Class<?>[] argTypes) throws Exception {

        Class<?> receiverType = argTypes[0];

        List<LibraryMethod> candidates = libraries.get(receiverType);

        if(candidates == null){
            return null;
        }

        for(LibraryMethod lm : candidates) {
            Method m = lm.m;
            ChipmunkLibrary lib = lm.lib;

            Class<?>[] candidatePTypes = m.getParameterTypes();

            if (candidatePTypes.length != argTypes.length) {
                continue;
            }

            if (!m.getName().equals(name)) {
                continue;
            }

            Class<?> retType = m.getReturnType();

            if (retType.equals(void.class) || returnType.isAssignableFrom(retType) || retType.isPrimitive()) {

                boolean matches = true;
                for (int i = 0; i < candidatePTypes.length; i++) {

                    Class<?> callPType = argTypes[i];
                    Class<?> candidatePType = candidatePTypes[i];

                    if (!candidatePType.isAssignableFrom(callPType)) {
                        matches = false;
                        break;
                    }
                }

                if(matches){
                    MethodHandle handle = lookup.unreflect(m);
                    if(!Modifier.isStatic(m.getModifiers())){
                        handle = handle.bindTo(lib);
                    }

                    return handle;
                }
            }
        }

        return null;
    }

    public void registerLibrary(ChipmunkLibrary library){
        try{
            for(Method m : library.getClass().getMethods()){
                Class<?>[] pTypes = m.getParameterTypes();

                if(pTypes.length == 0){
                    continue;
                }

                Class<?> receiverType = pTypes[0];
                if(!libraries.containsKey(receiverType)){
                    libraries.put(receiverType, new ArrayList<>());
                }

                libraries.get(receiverType).add(new LibraryMethod(m, library));
            }

        }catch(Throwable t){
            throw new RuntimeException("Failed to load library " + library.getClass().getName(), t);
        }
    }

    public void unregisterLibrary(ChipmunkLibrary library){
        try{
            for(Method m : library.getClass().getMethods()){
                Class<?>[] pTypes = m.getParameterTypes();

                if(pTypes.length == 0){
                    continue;
                }

                Class<?> receiverType = pTypes[0];
                if(!libraries.containsKey(receiverType)){
                    libraries.put(receiverType, new ArrayList<>());
                }

                libraries.get(receiverType).remove(m);
            }

        }catch(Throwable t){
            throw new RuntimeException("Failed to unload library " + library.getClass().getName(), t);
        }
    }
}

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

import chipmunk.runtime.Fiber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class NativeInvoker implements Invoker {

    protected final Method method;
    protected final Class<?>[] linkTypes;
    protected final ProxyFilter[] filters;

    public NativeInvoker(Method method, Class<?>[] linkTypes){
        this(method, linkTypes, null);
    }

    public NativeInvoker(Method method, Class<?>[] linkTypes, ProxyFilter[] filters){
        this.method = method;
        this.linkTypes = linkTypes;
        this.filters = filters;
        if(filters != null && filters.length != linkTypes.length){
            throw new IllegalArgumentException("Filter and link type array must be same size");
        }
    }

    @Override
    public Object invoke(Fiber fiber, Object target, int argCount, Object... args) throws Throwable {
        if(filters != null){
            for(int i = 0; i < args.length; i++){
                var filter = filters[i];
                if(filter != null){
                    args[i] = filter.filter(args[i]);
                }
            }
        }

        try{
            // We don't need to call validate() here because the method will handle parameter
            // type checking and conversion as necessary.
            // TODO - memory tracing
            return method.invoke(target, args);
        }catch (InvocationTargetException e){
            throw e.getTargetException();
        }
    }

    public Method getMethod(){
        return method;
    }

    public Class<?>[] getLinkTypes(){
        return linkTypes;
    }

    public boolean validate(Fiber fiber, Object target, int argCount, Object... args){

        if(!method.getDeclaringClass().isAssignableFrom(target.getClass()) && !validStaticTarget(method, target)){
            return false;
        }

        if(args.length != linkTypes.length){
            return false;
        }

        for(int i = 0; i < args.length; i++){
            if(!checkMatch(linkTypes[i], args[i])){
                return false;
            }
        }
        return true;
    }

    protected boolean validStaticTarget(Method m, Object target){
        return Modifier.isStatic(m.getModifiers()) && m.getDeclaringClass().equals(target);
    }

    protected boolean checkMatch(Class<?> type, Object param){
        return param == null || type.equals(param.getClass());
    }

}

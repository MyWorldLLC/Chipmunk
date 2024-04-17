/*
 * Copyright (C) 2024 MyWorld, LLC
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

import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.ChipmunkVM;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;

public class ProxyFilter {

    protected final ChipmunkVM vm;
    protected final Class<?> target;

    public ProxyFilter(ChipmunkVM vm, Class<?> target){
        this.vm = vm;
        this.target = target;
    }

    public Object filter(Object param) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if(param == null){
            return param;
        }

        return vm.proxy(target, param);
    }

    public static MethodHandle filterFor(MethodHandles.Lookup lookup, Class<?> target) throws NoSuchMethodException, IllegalAccessException{
        return filterFor(lookup, ChipmunkScript.getCurrentScript().getVM(), target);
    }

    public static MethodHandle filterFor(MethodHandles.Lookup lookup, ChipmunkVM vm, Class<?> target) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findVirtual(ProxyFilter.class, "filter", MethodType.methodType(Object.class, Object.class))
                .bindTo(new ProxyFilter(vm, target));
    }

}

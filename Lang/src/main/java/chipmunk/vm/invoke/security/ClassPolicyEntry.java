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

package chipmunk.vm.invoke.security;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class ClassPolicyEntry extends BasePolicyEntry {

    protected final Set<Class<?>> packages;

    public ClassPolicyEntry(){
        packages = new ConcurrentSkipListSet<>(Comparator.comparing(Class::getName));
    }

    public ClassPolicyEntry(SecurityMode mode){
        super(mode);
        packages = new ConcurrentSkipListSet<>(Comparator.comparing(Class::getName));
    }

    public ClassPolicyEntry(SecurityMode methodMode, SecurityMode fieldSetMode, SecurityMode fieldGetMode){
        super(methodMode, fieldSetMode, fieldGetMode);
        packages = new ConcurrentSkipListSet<>(Comparator.comparing(Class::getName));
    }

    public Set<Class<?>> getClasses(){
        return packages;
    }

    public ClassPolicyEntry add(Class<?> c){
        packages.add(c);
        return this;
    }

    public void remove(Class<?> c){
        packages.remove(c);
    }

    @Override
    public AccessEvaluation allowMethodCall(Object receiver, Method m, Object[] params){
        return super.evaluateMethodAccess(isSpecifiedByPolicy(m.getDeclaringClass()));
    }

    @Override
    public AccessEvaluation allowFieldSet(Object receiver, Field f, Object value){
        return super.evaluateFieldSetAccess(isSpecifiedByPolicy(f.getDeclaringClass()));
    }

    @Override
    public AccessEvaluation allowFieldGet(Object receiver, Field f){
        return super.evaluateFieldGetAccess(isSpecifiedByPolicy(f.getDeclaringClass()));
    }

    protected boolean isSpecifiedByPolicy(Class<?> receiverType){
        return packages.contains(receiverType);
    }

}

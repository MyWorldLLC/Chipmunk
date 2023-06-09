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

import chipmunk.runtime.ChipmunkClass;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.runtime.ChipmunkObject;
import chipmunk.runtime.NativeTypeLib;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LinkingPolicy {

    protected final SecurityMode mode;
    protected final CopyOnWriteArrayList<PolicyEntry> entries;

    public LinkingPolicy(SecurityMode mode){
        this.mode = mode;
        entries = new CopyOnWriteArrayList<>();

        if(mode == SecurityMode.DENYING){
            add(new ClassPolicyEntry(SecurityMode.ALLOWING).add(NativeTypeLib.class));
        }
    }

    public SecurityMode getDefaultMode(){
        return mode;
    }

    public LinkingPolicy add(PolicyEntry entry){
        if(entry == null){
            throw new IllegalArgumentException("Policy entry cannot be null");
        }

        entries.add(entry);
        return this;
    }

    public boolean remove(PolicyEntry entry){
        return entries.remove(entry);
    }

    public List<PolicyEntry> getEntries(){
        return entries;
    }

    public boolean allowInstantiation(Class<?> targetType, Object[] params){

        if(ChipmunkClass.class.isAssignableFrom(targetType)){
            return true;
        }

        for(PolicyEntry e : entries){
            AccessEvaluation eval = e.allowInstantiation(targetType, params);
            if(eval != AccessEvaluation.UNSPECIFIED){
                return eval == AccessEvaluation.ALLOWED;
            }
        }

        return mode == SecurityMode.ALLOWING;
    }

    public boolean allowMethodCall(Object target, Method method, Object[] params){

        if(method.getAnnotation(AllowChipmunkLinkage.class) != null ||
                method.getDeclaringClass().getDeclaredAnnotation(AllowChipmunkLinkage.class) != null){
            return true;
        }

        for(PolicyEntry e : entries){
            AccessEvaluation eval = e.allowMethodCall(target, method, params);
            if(eval != AccessEvaluation.UNSPECIFIED){
                return eval == AccessEvaluation.ALLOWED;
            }
        }

        return mode == SecurityMode.ALLOWING;
    }

    public boolean allowFieldSet(Object target, Field field, Object value){

        if(field.getAnnotation(AllowChipmunkLinkage.class) != null ||
            field.getDeclaringClass().getDeclaredAnnotation(AllowChipmunkLinkage.class) != null){
            return true;
        }

        for(PolicyEntry e : entries){
            AccessEvaluation eval = e.allowFieldSet(target, field, value);
            if(eval != AccessEvaluation.UNSPECIFIED){
                return eval == AccessEvaluation.ALLOWED;
            }
        }

        return mode == SecurityMode.ALLOWING;
    }

    public boolean allowFieldGet(Object target, Field field){

        if(field.getAnnotation(AllowChipmunkLinkage.class) != null ||
                field.getDeclaringClass().getDeclaredAnnotation(AllowChipmunkLinkage.class) != null){
            return true;
        }

        for(PolicyEntry e : entries){
            AccessEvaluation eval = e.allowFieldGet(target, field);
            if(eval != AccessEvaluation.UNSPECIFIED){
                return eval == AccessEvaluation.ALLOWED;
            }
        }

        return mode == SecurityMode.ALLOWING;
    }
}

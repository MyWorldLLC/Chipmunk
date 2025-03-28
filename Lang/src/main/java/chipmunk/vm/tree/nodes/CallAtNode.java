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

package chipmunk.vm.tree.nodes;

import chipmunk.runtime.CClass;
import chipmunk.runtime.CMethod;
import chipmunk.runtime.CObject;
import chipmunk.runtime.Fiber;
import chipmunk.vm.ChipmunkVM;
import chipmunk.vm.tree.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CallAtNode implements Node {
    int locals;
    Node obj;
    String targetName;
    Node[] args;
    boolean isOperator;

    public CallAtNode(int locals, Node obj, String name, Node... args) {
        this.locals = locals;
        this.args = args;
        this.obj = obj;
        this.targetName = name;
    }

    public static CallAtNode operator(int locals, Node obj, String name, Node... args){
        var node = new CallAtNode(locals, obj, name, args);
        node.isOperator = true;
        return node;
    }

    // TODO - support suspensions
    @Override
    public Object execute(Fiber ctx){
        Object target = null;
        try{
            target = obj.execute(ctx);
        }catch (Exception e){
            ctx.suspendStateless(e, (c) -> {/* TODO */return null;});
        }

        if(target instanceof CObject cObj){

            ctx.setLocal(locals, cObj);
            for (int i = 0; i < args.length; i++) {
                // TODO = suspension
                ctx.setLocal(i + locals + 1, args[i].execute(ctx));
            }

            // TODO - method caching

            var method = cObj.cls.getMethod(cObj, targetName, args.length + 1);
            ctx.preCall(locals);
            var result = method.code.execute(ctx);
            ctx.postCall();
            return result;

        }else{
            try {
                Object[] readyArgs;
                int arg0Index = 0;
                if(isOperator){
                    readyArgs = new Object[args.length + 1];
                    arg0Index = 1;
                    readyArgs[0] = target;
                }else{
                    readyArgs = new Object[args.length];
                }
                for (int i = arg0Index; i < readyArgs.length; i++) {
                    // TODO = suspension
                    readyArgs[i] = args[i - arg0Index].execute(ctx);
                }
                var targetType = target.getClass();
                var argTypes = CallUtil.argTypes(readyArgs);

                var cached = ctx.getNodeCache(this, CachedNativeCall.class, () -> cacheInit(ctx.vm, targetType, readyArgs, argTypes));
                if(!cached.checkCall(targetType, argTypes)){
                    cached = ctx.replaceNodeCache(this, () -> cacheInit(ctx.vm, targetType, readyArgs, argTypes));
                }
                return cached.method.invoke(target, readyArgs);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private CachedNativeCall cacheInit(ChipmunkVM vm, Class<?> targetType, Object[] args, Class<?>[] argTypes){
        var method = resolveMethod(vm, targetType, args, argTypes);
        if(method != null){
            method.setAccessible(true);
        }
        return new CachedNativeCall(targetType, argTypes, method);
    }

    private Method resolveMethod(ChipmunkVM vm, Class<?> targetType, Object[] args, Class<?>[] argTypes){
        try {
            // TODO - we actually want the linker for the currently running script
            return vm.getDefaultLinker().getMethod(targetType, targetName, args, argTypes, true);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private record CachedNativeCall(Class<?> targetType, Class<?>[] argTypes, Method method){

        public boolean checkCall(Class<?> targetType, Class<?>[] argTypes){
            return targetType.equals(this.targetType) && Arrays.equals(this.argTypes, argTypes);
        }

    }

    // Arg types must be Object[] because it can be a mix of Java classes & CClasses.
    private record CachedCall(CClass targetType, Object[] argTypes, CMethod method){

        public boolean checkCall(CClass targetType, Object[] argTypes){
            return targetType.equals(this.targetType) && Arrays.equals(this.argTypes, argTypes);
        }

    }
}

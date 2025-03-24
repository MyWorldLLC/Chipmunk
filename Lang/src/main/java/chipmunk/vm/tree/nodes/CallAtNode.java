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

import chipmunk.vm.tree.CObject;
import chipmunk.vm.tree.CallUtil;
import chipmunk.vm.tree.Fiber;
import chipmunk.vm.tree.Node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CallAtNode implements Node {
    int locals;
    Node obj;
    String targetName;
    Node[] args;

    public CallAtNode(int locals, Node obj, String name, Node... args) {
        this.locals = locals;
        this.args = args;
        this.obj = obj;
        this.targetName = name;
    }

    // TODO - support suspensions
    @Override
    public Object execute(Fiber ctx){
        var target = obj.execute(ctx);

        if(target instanceof CObject cObj){

            ctx.setLocal(locals, cObj);
            for (int i = 0; i < args.length; i++) {
                ctx.setLocal(i + locals + 1, args[i].execute(ctx));
            }

            var method = cObj.cls.getMethod(cObj, targetName, args.length);
            ctx.preCall(locals);
            var result = method.code.execute(ctx);
            ctx.postCall();
            return result;

        }else{
            try {
                var readyArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    readyArgs[i] = args[i].execute(ctx);
                }
                var targetType = target.getClass();
                var argTypes = CallUtil.argTypes(readyArgs);

                var cached = ctx.getNodeCache(this, CachedNativeCall.class, () -> cacheInit(targetType, argTypes));
                if(!cached.checkCall(targetType, argTypes)){
                    cached = ctx.replaceNodeCache(this, () -> cacheInit(targetType, argTypes));
                }
                return cached.method.invoke(target, readyArgs);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private CachedNativeCall cacheInit(Class<?> targetType, Class<?>[] argTypes){
        var method = resolveMethod(targetType, argTypes);
        if(method != null){
            method.setAccessible(true);
        }
        return new CachedNativeCall(targetType, argTypes, method);
    }

    private Method resolveMethod(Class<?> targetType, Class<?>[] argTypes){
        // TODO - VM security checks
        // TODO - varargs & static methods
        try {
            return targetType.getMethod(targetName, argTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private record CachedNativeCall(Class<?> targetType, Class<?>[] argTypes, Method method){

        public boolean checkCall(Class<?> targetType, Class<?>[] argTypes){
            return targetType.equals(this.targetType) && Arrays.equals(this.argTypes, argTypes);
        }

    }
}

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

package chipmunk.vm;

import chipmunk.runtime.CMethod;
import chipmunk.runtime.CModule;
import chipmunk.runtime.CObject;
import chipmunk.runtime.Suspension;
import chipmunk.vm.invoke.CachedChipmunkCall;
import chipmunk.vm.invoke.CachedNativeCall;
import chipmunk.vm.invoke.CallUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static chipmunk.vm.BytecodeInterpreter.dispatch;

/**
 * Fiber execution context. Note that interrupt() is the only method that can
 * be called from a thread that is not the currently executing thread.
 */
public final class Fiber {

    private static final CMethod HOST_INVOKER = new CMethod(new CModule("<host>"), "<invoker>", 0, new byte[0]);

    private record Cache(CMethod method, int ip, Object cacheValue){}

    public final ChipmunkScript script;

    private int[] frames = new int[40];
    private int framePtr = 0;
    private int callStackPtr = 0;

    private Object[] locals = new Object[200];
    private final Cache[] caches = new Cache[100];

    public final ArrayList<Object> stack = new ArrayList<>();

    protected volatile boolean interrupt;

    Deque<Suspension> suspensions = new ArrayDeque<>();

    public Fiber(ChipmunkScript script){
        this.script = script;
    }

    public void prepareCall(int callerLocals, int argCount) {
        frames[framePtr] = callStackPtr;
        framePtr++;
        callStackPtr += callerLocals;
        // Note: this *must* happen after stack pointer is adjusted!
        for(int i = argCount - 1; i >= 0; i--){
            setLocal(i, pop());
        }
    }

    public void postCall() {
        framePtr--;
        callStackPtr = frames[framePtr];
    }

    public Object setLocal(int local, Object value) {
        locals[callStackPtr + local] = value;
        return value;
    }

    public Object getLocal(int local) {
        return locals[callStackPtr + local];
    }

    public void interrupt(){
        interrupt = true;
    }

    public boolean interrupted() {
        return interrupt;
    }

    public void initialize(CMethod entryPoint, Object... args){
        if(!suspensions.isEmpty()){
            throw new IllegalStateException("This fiber has already been initialized");
        }
        pushArgs(args);
        suspendFrame(entryPoint, 0);
    }

    public void suspendFrame(CMethod method, int ip){
        suspensions.addLast(new Suspension(method, ip));
    }

    public Suspension resumeFrame(){
        return suspensions.removeFirst();
    }

    public boolean hasSuspension(){
        return !suspensions.isEmpty();
    }

    public Object hostInvoke(Object target, String name, Object[] params){
        push(target);
        prepareCall(0, params.length);
        pushArgs(params);
        return invokeValue(HOST_INVOKER, 0, name, params.length, false);
    }

    public void invoke(CMethod method, int ip, String name, int count, boolean operator){
        var target = peek(count);
        if(target instanceof CObject cObj){

            count++; // + 1 for self

            // TODO - caching
            //System.out.println("CObject call " + name);

            var callTarget = cObj.cls.getMethod(cObj, name, count + 1);

            prepareCall(method.localCount, count + 1);
            try{
                dispatch(this, callTarget, 0);
            }finally{
                postCall();
            }
        }else{
            try {
                // Operators are always defined as static methods, so we do this to
                // shift what would normally be the call target into the arguments array.
                Object[] args = new Object[operator ? count + 1 : count];
                for (int i = args.length - 1; i >= 0; i--) {
                    args[i] = pop();
                }

                //System.out.println("Native call " + name);

                // TODO - throw a Chipmunk-specific NPE with a Chipmunk stack trace
                var targetType = target.getClass();
                var argTypes = CallUtil.types(args);

                CachedNativeCall cached = getCache(method, ip);

                if (cached == null || !cached.matches(targetType, name, argTypes)) {
                    var m = script.getVM().createLinker()
                            .getMethod(targetType, name, args, argTypes, true);

                    if(m == null){
                        throw new NullPointerException("Cannot find method %s.%s(%s)".formatted(targetType.getName(), name, Arrays.toString(argTypes)));
                    }

                    cached = new CachedNativeCall(targetType, name, argTypes, m);
                    setCache(method, ip, cached);
                }

                // TODO - catch suspension and throw a fatal exception if the native method is not annotated
                // as supporting suspension.
                //var callResult = cached.method().invoke(target, args);
                //System.out.println("Result: " + callResult);
                push(cached.method().invoke(target, args));
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Object invokeValue(CMethod method, int ip, String name, int count, boolean operator){
        invoke(method, ip, name, count, operator);
        return pop();
    }

    public void push(Object o){
        stack.add(o);
    }

    private void pushArgs(Object[] args){
        for(var arg : args){
            push(arg);
        }
    }

    public Object pop(){
        return stack.removeLast();
    }

    public Object peek(){
        return stack.getLast();
    }

    public Object peek(int fromTop){
        return stack.get(stack.size() - 1 - fromTop);
    }

    private int fastHash(CMethod method, int ip){
        // Hash combination borrowed from Java's Arrays.hash() implementation
        return Math.abs(31 * ip + (31 * method.hashCode()));
    }

    private int findCacheIndex(CMethod method, int ip){
        var idx = fastHash(method, ip) % caches.length;
        // Cap the search space to within 10 entries of the hashed location
        for(int i = idx; i < caches.length && (i - idx) < 10; i++){
            var cache = caches[i];
            // Use reference equals because it's faster and is guaranteed
            // never to false positive
            if(cache == null || (cache.method() == method && cache.ip() == ip)){
                return i;
            }
        }
        // We didn't find an existing or available exact cache location, so
        // randomly evict and replace a prior cache entry
        idx = (31 * caches.length + idx) % caches.length;
        caches[idx] = null;
        return idx;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCache(CMethod method, int ip){
        var entry = caches[findCacheIndex(method, ip)];
        return entry == null ? null : (T) entry.cacheValue();
    }

    public <T> void setCache(CMethod method, int ip, T value){
        caches[findCacheIndex(method, ip)] = new Cache(method, ip, value);
    }

    public void flushCaches(){
        Arrays.fill(caches, null);
    }

}

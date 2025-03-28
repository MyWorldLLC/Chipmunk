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

package chipmunk.runtime;

import chipmunk.vm.ChipmunkVM;
import chipmunk.vm.invoke.security.SecurityMode;
import chipmunk.vm.tree.Node;
import chipmunk.vm.tree.NodePartial;
import chipmunk.vm.tree.StatelessNodePartial;
import chipmunk.vm.tree.Suspension;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

import static chipmunk.vm.tree.Conversions.toInt;

/**
 * Fiber execution context. Note that interrupt() is the only method that can
 * be called from a thread that is not the currently executing thread.
 */
public class Fiber {
    public final ChipmunkVM vm;
    int[] frame = new int[40];
    int framePtr = 0;
    int stackPtr = 0;
    Object[] locals = new Object[200];
    Object[] nodeCaches = new Object[20];

    volatile boolean interrupt;
    public boolean _return;

    Deque<Suspension> suspensions = new ArrayDeque<>();

    public Fiber(){
        this(new ChipmunkVM(SecurityMode.DENYING));
    }

    public Fiber(ChipmunkVM vm){
        this.vm = vm;
    }

    public void preCall(int locals) {
        frame[framePtr] = stackPtr;
        framePtr++;
        stackPtr += locals;
    }

    public void postCall() {
        framePtr--;
        stackPtr = frame[framePtr];
        _return = false;
    }

    public Object setLocal(int local, Object value) {
        locals[stackPtr + local] = value;
        return value;
    }

    public Object getLocal(int local) {
        return locals[stackPtr + local];
    }

    public void interrupt(){
        interrupt = true;
    }

    public boolean checkInterrupt() {
        return _return || interrupt;
    }

    public void resume(Fiber ctx) {
        var suspension = suspensions.pop();
        while (suspension != null) {
            var value = suspension.value();
            // TODO - add support for exception handlers to suspensions. When executing a suspension,
            // if an exception is thrown walk back through the suspension stack and execute the nearest handler.
            for (int i = toInt(suspension.state()); i < suspension.states().length; i++) {
                try {
                    value = suspension.states()[i].execute(ctx, i);
                } catch (Exception e) {
                    ctx.suspend(e, value, Arrays.stream(suspension.states()).skip(i - 1).toArray(NodePartial[]::new));
                }
            }
            suspension = suspensions.pop();
        }

    }

    public void suspendStateless(Throwable t, StatelessNodePartial... resumables) throws RuntimeException {
        suspend(t, 0, resumables);
    }

    public void suspendStateless(Throwable t, NodePartial... resumables) throws RuntimeException {
        suspend(t, 0, resumables);
    }

    public void suspend(Throwable t, Object state, NodePartial... resumables) throws RuntimeException {
        suspensions.add(new Suspension(state, resumables));
        throw new RuntimeException(t);
    }

    public <T> T getNodeCache(Node node, Class<T> type, Supplier<T> init){
        var idx = Objects.hashCode(node) % nodeCaches.length;
        var cached = nodeCaches[idx];
        if(cached == null || !type.equals(cached.getClass())){
            cached = init.get();
            nodeCaches[idx] = cached;
        }
        return type.cast(cached);
    }

    public <T> T replaceNodeCache(Node node, Supplier<T> init){
        var idx = Objects.hashCode(node) % nodeCaches.length;
        var cache = init.get();
        nodeCaches[idx] = cache;
        return cache;
    }
}

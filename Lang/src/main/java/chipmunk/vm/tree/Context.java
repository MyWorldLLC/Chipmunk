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

package chipmunk.vm.tree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Fiber execution context. Note that interrupt() is the only method that can
 * be called from a thread that is not the currently executing thread.
 */
public class Context {
    int[] frame = new int[40];
    int framePtr = 0;
    int stackPtr = 0;
    long[] locals = new long[200];
    Object[] localRefs = new Object[200];

    volatile boolean interrupt;
    public boolean _return;

    Deque<Suspension> suspensions = new ArrayDeque<>();

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

    public long setLocal(int local, long value) {
        locals[stackPtr + local] = value;
        return value;
    }

    public long getLocal(int local) {
        return locals[stackPtr + local];
    }

    public void interrupt(){
        interrupt = true;
    }

    public boolean checkInterrupt() {
        return interrupt;
    }

    public void resume(Context ctx) {
        var suspension = suspensions.pop();
        while (suspension != null) {
            var value = suspension.value();
            // TODO - add support for exception handlers to suspensions. When executing a suspension,
            // if an exception is thrown walk back through the suspension stack and execute the nearest handler.
            for (int i = (int) suspension.state(); i < suspension.states().length; i++) {
                try {
                    value = suspension.states()[i].execute(ctx, i);
                } catch (Exception e) {
                    ctx.suspend(e, value, Arrays.stream(suspension.states()).skip(i - 1).toArray(NodePartial[]::new));
                }
            }
            suspension = suspensions.pop();
        }

    }

    public void suspendStateless(Throwable t, NodePartial... resumables) throws RuntimeException {
        suspend(t, 0, resumables);
    }

    public void suspend(Throwable t, long state, NodePartial... resumables) throws RuntimeException {
        suspensions.add(new Suspension(state, resumables));
        throw new RuntimeException(t);
    }
}

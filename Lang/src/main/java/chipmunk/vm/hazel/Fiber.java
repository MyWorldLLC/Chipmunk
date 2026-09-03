/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.vm.hazel;

import chipmunk.runtime.CMethod;

public final class Fiber {

    public static final int DEFAULT_INITIAL_STACK = 1024;

    public enum State {
        RUNNABLE,
        BLOCKED,
        TRAPPED
    }

    private final HazelVM vm;
    private final CMethod startMethod;
    private State state;
    private volatile boolean yieldRequested = false;
    public double[] stack;

    public int ip;
    public int bp;
    public int sp;

    public Fiber(HazelVM vm, CMethod startMethod) {
        this(vm, startMethod, DEFAULT_INITIAL_STACK);
    }

    public Fiber(HazelVM vm, CMethod startMethod, int initialStack){
        this.vm = vm;
        this.startMethod = startMethod;
        stack = new double[initialStack];
        state = State.RUNNABLE;
    }

    public HazelVM vm() {
        return vm;
    }

    public CMethod startMethod(){
        return startMethod;
    }

    public Object[] constants(){
        return startMethod.module().constants(); // TODO - this needs to be from the current method on top of the call stack
    }

    public void state(State state){
        this.state = state;
    }

    public State state(){
        return state;
    }

    public int _return(){
        // TODO - keep stack of method code that's been entered, pop the stack here and restore ip/bp/sp accordingly
        return Integer.MIN_VALUE;
    }

    public double[] stack(){
        return stack;
    }

    public double lastReturned(){
        return stack[startMethod().localCount()]; // TODO - use last frame info
    }

    public boolean isYieldRequested(){
        return yieldRequested;
    }

    public void yield(){
        yieldRequested = true;
    }
}

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

    public static final int RETURN_SIGNAL = -Integer.MAX_VALUE;

    public static final int DEFAULT_INITIAL_STACK = 1024;
    public static final int DEFAULT_CALL_FRAMES = 32;

    public enum State {
        RUNNABLE,
        BLOCKED,
        TRAPPED
    }

    public static class Frame {
        public int ip;
        public int bp;
        public int sp;
        public CMethod method;
    }

    private final HazelVM vm;
    private final CMethod startMethod;
    private State state;
    private volatile boolean yieldRequested = false;
    public double[] stack;
    public Frame[] callFrames;
    public int callFramePtr;
    private Frame currentFrame;
    private Object[] constants;

    public int ip;
    public int bp;
    public int sp;

    public Fiber(HazelVM vm, CMethod startMethod) {
        this(vm, startMethod, DEFAULT_INITIAL_STACK, DEFAULT_CALL_FRAMES);
    }

    public Fiber(HazelVM vm, CMethod startMethod, int initialStack, int initialCallFrames){
        this.vm = vm;
        this.startMethod = startMethod;
        stack = new double[initialStack];
        state = State.RUNNABLE;

        callFrames = new Frame[initialCallFrames];
        callFramePtr = 0;
    }

    public HazelVM vm() {
        return vm;
    }

    public CMethod startMethod(){
        return startMethod;
    }

    public Object[] constants(){
        return constants;
    }

    public void state(State state){
        this.state = state;
    }

    public State state(){
        return state;
    }

    public double[] stack(){
        return stack;
    }

    public Frame pushAndPopulateFrame(CMethod callMethod, int ip, int bp, int sp){
        var frame = pushFrame();
        frame.bp = bp;
        frame.sp = sp;
        frame.ip = ip;
        frame.method = callMethod;
        constants = currentFrame.method.module().constants();
        return frame;
    }

    public Frame pushFrame(){
        var frame = callFrames[callFramePtr];
        if(frame == null){
            frame = new Frame();
            // TODO - support expanding call stack
            callFrames[callFramePtr] = frame;
        }
        currentFrame = frame;
        callFramePtr++;
        return frame;
    }

    public Frame currentFrame(){
        return currentFrame;
    }

    public void popFrame(){
        callFramePtr--;
        currentFrame = callFrames[callFramePtr];
        if(currentFrame != null){
            constants = currentFrame.method.module().constants();
        }

    }

    public double lastReturned(){
        var lastFrame = callFrames[callFramePtr];
        return stack[lastFrame.bp + lastFrame.method.localCount()];
    }

    public int callStackDepth(){
        return callFramePtr;
    }

    public boolean completed(){
        return callStackDepth() == 0;
    }

    public boolean isYieldRequested(){
        return yieldRequested;
    }

    public void yield(){
        yieldRequested = true;
    }
}

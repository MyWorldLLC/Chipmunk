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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

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

        public static final int FRAME_SIZE = 4 * 3 + 2 * 8; // 3 ints, 2 object references

        public int ip;
        public int bp;
        public int sp;
        public CMethod method;
        public NativeContinuation continuation;

        public void clearNativeContinuation(){
            continuation = null;
        }
    }

    private final HazelVM vm;
    private final CMethod startMethod;

    private Fiber blockedBy;
    private Fiber blocking;

    private State state;
    public double[] stack;
    public Frame[] callFrames;
    public int callFramePtr;

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

    public void state(State state){
        this.state = state;
    }

    public State state(){
        return state;
    }

    public double[] stack(){
        return stack;
    }

    public Frame pushCallFrame(CMethod callMethod, int bp){
        var frame = pushFrame();
        frame.bp = bp;
        frame.ip = 0;
        frame.method = callMethod;
        return frame;
    }

    public Frame pushFrame(){
        var frame = callFrames[callFramePtr];
        if(frame == null){
            frame = new Frame();
            try{
                callFrames[callFramePtr] = frame;
            }catch(ArrayIndexOutOfBoundsException e){
                // TODO - support call stack limit, throw error when stack depth exceeded.
                var tmp = new Frame[callFrames.length * 2];
                System.arraycopy(callFrames, 0, tmp, 0, callFrames.length);
                callFrames = tmp;
                callFrames[callFramePtr] = frame;
            }
        }
        callFramePtr++;
        return frame;
    }

    public Frame pushCallFrame(int ip, int bp, int sp){
        return pushCallFrame(null, bp);
    }

    public Frame currentFrame(){
        return callFrames[callFramePtr - 1];
    }

    public void popFrame(){
        callFramePtr--;
    }

    public double lastReturned(){
        var lastFrame = callFrames[callFramePtr];
        return stack[lastFrame.bp + lastFrame.method.localCount()];
    }

    public void markExceptionTraceTop(){
        if(callFramePtr < callFrames.length){
            callFrames[callFramePtr] = null;
        }
    }

    public double readArg(int bp, int sp, int argCount, int arg){
        return stack[bp + sp - argCount + arg];
    }

    public void pushResult(int bp, int sp, int argCount, double result){
        stack[bp + sp - argCount] = result;
    }

    public void continueWith(NativeContinuation continuation){
        currentFrame().continuation = continuation;
    }

    public int callStackDepth(){
        return callFramePtr;
    }

    public boolean completed(){
        return callStackDepth() == 0;
    }

    public void block(Fiber f){
        f.state = State.BLOCKED;
        f.blockedBy = this;
        blocking = f;
    }

    public void unblock(){
        blocking.state = State.RUNNABLE;
        blocking.blockedBy = null;
        blocking = null;
    }

    public boolean isBlocked(){
        return blocking.state == State.BLOCKED;
    }

    public Fiber blockedBy(){
        return blockedBy;
    }

    public boolean isBlocking(){
        return blocking != null;
    }

    public Stream<Frame> stackTrace(){
        return Arrays.stream(callFrames)
                .filter(Objects::nonNull);
    }

}

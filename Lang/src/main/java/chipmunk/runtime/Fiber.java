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

package chipmunk.runtime;

import java.util.ArrayDeque;
import java.util.Deque;

public class Fiber {

    public enum Status {
        RUNNING, READY, SUSPENDED, BLOCKED
    }

    public static class Frame {

        public final String method;
        public final int suspensionPoint;
        public final Object[] stack;
        public final Object[] locals;

        public Frame(String method, int suspensionPoint, int stackSize, int localsSize) {
            this.method = method;
            this.suspensionPoint = suspensionPoint;
            this.stack = new Object[stackSize];
            this.locals = new Object[localsSize];
        }

        public String method(){
            return method;
        }
    }

    protected final Deque<Frame> frames = new ArrayDeque<>();

    protected volatile Status status;
    protected volatile boolean yieldRequested;
    private boolean isRewinding;

    public void yield(){
        yieldRequested = true;
    }

    public boolean isYieldRequested(){
        return yieldRequested;
    }

    public void status(Status status){
        this.status = status;
    }

    public Status status(){
        return status;
    }

    public void unwind(Frame frame){
        frames.push(frame);
    }

    public Frame rewind(){
        var frame = frames.pop();
        if(frames.isEmpty()){
            isRewinding = false;
        }
        return frame;
    }

    public void startRewinding(){
        isRewinding = true;
    }

    public boolean isRewinding(){
        return isRewinding;
    }

    public Deque<Frame> frames(){
        return frames;
    }
}

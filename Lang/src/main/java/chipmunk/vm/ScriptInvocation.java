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

package chipmunk.vm;

import java.util.concurrent.CompletableFuture;

public class ScriptInvocation {

    protected volatile long queueTime;
    protected volatile long startTime;
    protected final float priority;
    protected final ChipmunkScript script;
    protected final CompletableFuture<Object> future;

    public ScriptInvocation(long queueTime, ChipmunkScript script, float priority, CompletableFuture<Object> future){
        this.queueTime = queueTime;
        this.script = script;
        this.priority = priority;
        this.future = future;
    }

    public float priority(){
        return priority;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getQueueTime() {
        return queueTime;
    }

    public void setQueueTime(long queueTime) {
        this.queueTime = queueTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public ChipmunkScript getScript() {
        return script;
    }

    public CompletableFuture<Object> getFuture(){
        return future;
    }
}

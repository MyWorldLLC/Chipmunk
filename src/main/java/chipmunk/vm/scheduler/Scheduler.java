/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk.vm.scheduler;

import chipmunk.vm.ChipmunkScript;

import java.util.concurrent.ConcurrentHashMap;

public class Scheduler {

    protected final ConcurrentHashMap<Long, ScriptInvocation> invocations;
    protected final Thread schedulingThread;

    public Scheduler(){
        invocations = new ConcurrentHashMap<>();
        schedulingThread = new Thread(this::schedule, "Chipmunk Scheduler");
    }

    public void start(){
        schedulingThread.start();
    }

    public void shutdown(){
        schedulingThread.interrupt();
    }

    public void notifyInvocationBegan(ChipmunkScript script){
        invocations.putIfAbsent(script.getId(), new ScriptInvocation(script));
    }

    public void notifyInvocationEnded(ChipmunkScript script){
        invocations.remove(script.getId());
    }

    private void schedule(){
        while(!Thread.interrupted()){
            // TODO - yield scripts that have run for too long
        }
        Thread.currentThread().interrupt();
    }

}

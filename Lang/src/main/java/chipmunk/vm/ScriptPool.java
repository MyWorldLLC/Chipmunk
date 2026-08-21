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

import chipmunk.vm.scheduler.Scheduler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ScriptPool {

    protected final ConcurrentHashMap<Long, ChipmunkScript> activeScripts;
    protected final AtomicLong scriptIds;
    protected final ExecutorService scriptExecutor;
    protected final Scheduler scheduler;

    public ScriptPool() {
        activeScripts = new ConcurrentHashMap<>();
        scriptIds = new AtomicLong();
        scriptExecutor = Executors.newVirtualThreadPerTaskExecutor();
        scheduler = new Scheduler();
    }

    public void start(){
        scheduler.start();
    }

    public void shutdown(){
        scriptExecutor.shutdown();
        scheduler.shutdown();
    }

    public ChipmunkScript newScript(ChipmunkVM vm){
        var script = new ChipmunkScript(vm, scriptIds.incrementAndGet());
        script.moduleLoader(new ModuleLoader());
        activeScripts.put(script.id(), script);
        return script;
    }

    public CompletableFuture<Object> runInScriptPool(ChipmunkScript script, Callable<Object> task){
        scheduler.notifyQueuedForInvocation(script);
        return CompletableFuture.supplyAsync(() -> {
            try {
                scheduler.notifyInvocationBegan(script);
                ChipmunkScript.setCurrentScript(script);
                return task.call();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                scheduler.notifyInvocationEnded(script);
                ChipmunkScript.setCurrentScript(null);
            }
        }, scriptExecutor);
    }

    public CompletableFuture<Void> runInScriptPool(ChipmunkScript script, Runnable task){
        return runInScriptPool(script, () -> {
            task.run();
            return null;
        }).thenApply(_ -> null);
    }

}

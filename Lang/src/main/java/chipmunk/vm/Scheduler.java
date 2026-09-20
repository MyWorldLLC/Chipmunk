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

import chipmunk.vm.hazel.HazelVM;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;

public class Scheduler {

    public static final int DEFAULT_POLLING_PERIOD = 1;
    public static final int DEFAULT_MINIMUM_EXEC_WINDOW = 1;
    public static final int DEFAULT_REPRIORITIZATION_FREQUENCY = 2;

    protected final ConcurrentHashMap<Long, ScriptInvocation> invocations;
    protected final Thread schedulingThread;
    protected final PriorityBlockingQueue<ScriptInvocation> scriptQueue;
    protected final PriorityFunction priorityFunction;

    protected final ExecutorService threads;

    protected volatile int minimumExecWindow;
    protected volatile int pollingPeriod;
    protected volatile int reprioritizationFrequency;
    protected final int threadCount;
    protected volatile boolean shutdownRequested;

    public Scheduler(int threadCount, ExecutorService threads, PriorityFunction priorityFunction){
        invocations = new ConcurrentHashMap<>();
        scriptQueue = new PriorityBlockingQueue<>(10, Comparator.comparing(ScriptInvocation::priority));
        this.priorityFunction = priorityFunction;
        this.threads = threads;

        schedulingThread = new Thread(this::schedule, "Chipmunk Scheduler");
        pollingPeriod = DEFAULT_POLLING_PERIOD;
        minimumExecWindow = DEFAULT_MINIMUM_EXEC_WINDOW;
        reprioritizationFrequency = DEFAULT_REPRIORITIZATION_FREQUENCY;
        this.threadCount = threadCount;
    }

    public void start(){
        schedulingThread.start();
        for(var i = 0; i < threadCount; i++){
            threads.execute(this::run);
        }
    }

    public void shutdown(){
        schedulingThread.interrupt();
    }

    public int getPollingPeriod() {
        return pollingPeriod;
    }

    public void setPollingPeriod(int pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

    public int getMinimumExecWindow() {
        return minimumExecWindow;
    }

    public void setMinimumExecWindow(int minimumExecWindow) {
        if(minimumExecWindow <= 0){
            throw new IllegalArgumentException("Minimum execution window must be greater than 0");
        }
        this.minimumExecWindow = minimumExecWindow;
    }

    public int getReprioritizationFrequency() {
        return reprioritizationFrequency;
    }

    public void setReprioritizationFrequency(int reprioritizationFrequency) {
        this.reprioritizationFrequency = reprioritizationFrequency;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int queueDepth(){
        return scriptQueue.size();
    }

    public CompletableFuture<Object> enqueue(ChipmunkScript script){
        if(shutdownRequested){
            throw new IllegalStateException("Scheduler is shutting down");
        }
        var future = new CompletableFuture<>();
        enqueueInternal(script, future);
        return future;
    }

    private void enqueueInternal(ChipmunkScript script, CompletableFuture<Object> future){
        scriptQueue.add(new ScriptInvocation(System.nanoTime(), script, priorityFunction.priority(script), future));
    }

    private void schedule(){

        var reprioritizationCounter = 0;

        while(!Thread.interrupted()){
            // Yield scripts that have run for too long
            for(var entry : invocations.entrySet()){
                var invocation = entry.getValue();
                var script = invocation.getScript();
                var elapsed = elapsedMillis(invocation);
                if(elapsed >= minimumExecWindow){
                    if(shouldYield(elapsed, invocation.priority())){
                        script.yield();
                        // Ignore shutdown status so that invocations requested before shutdown complete.
                        enqueueInternal(script, invocation.getFuture());
                    }
                }
            }

            reprioritizationCounter++;
            if(reprioritizationCounter >= reprioritizationFrequency){
                reprioritizationCounter = 0;

                // Rebuild the queue to ensure that scripts that have been enqueued the longest get the highest priority.
                var depth = queueDepth(); // This is a concurrent queue, so we process as many entries as were present when we started
                for(int i = 0; i < depth; i++){
                    var invocation = scriptQueue.poll();
                    // Workers could potentially run invocations faster than we rebuild the queue, so always check for null.
                    if(invocation == null){
                        break;
                    }

                    var millisQueued = (System.nanoTime() - invocation.getQueueTime()) / 1_000_000;
                    var windowsMissed = millisQueued / minimumExecWindow;
                    var basePriority = priorityFunction.priority(invocation.getScript());
                    // Exponentially increase priority with every missed window. Note that a growth constant of 0.2 means
                    // that a script that's missed:
                    // 2 execution windows -> ~3x higher than base priority
                    // 6 execution windows -> ~20x higher than base priority
                    var newPriority = (float) (basePriority * Math.exp(0.2 * windowsMissed));
                    scriptQueue.add(new ScriptInvocation(invocation.getQueueTime(), invocation.getScript(), newPriority, invocation.getFuture()));
                }
            }

            try {
                Thread.sleep(pollingPeriod);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch(Throwable t){
                // This keeps the scheduler alive if checking/yield throws an exception
            }
        }
    }

    private int elapsedMillis(ScriptInvocation invocation){
        return (int) (System.nanoTime() - invocation.getStartTime()) / 1_000_000;
    }

    private boolean shouldYield(int execTime, float execWeight){
        var next = scriptQueue.peek();
        if(next != null){
            // This uses a decaying exponential, where time is the number of milliseconds since the executing script began.
            // The decay constant of 0.9 means that the priority cuts approximately in half after 1 ms of execution time, and
            // decays exponentially towards zero after that. This time decayed priority is compared to the priority of the next
            // item in line, meaning that relatively high priority tasks won't always be yielded immediately.
            var decayedPriority = execWeight * Math.exp(-0.9*execTime);
            return decayedPriority <= next.priority();
        }
        return false;
    }

    private void run(){
        // Make sure to fully drain the queue before exiting
        while(!shutdownRequested || (shutdownRequested && !scriptQueue.isEmpty())){
            var invocation = scriptQueue.poll();
            if(invocation != null){
                var script = invocation.getScript();
                invocation.setStartTime(System.nanoTime());
                var running = invocations.put(script.getId(), invocation);
                if(running != null){
                    // Verify that this script isn't currently executing on another runner. If it is, re-enqueue - this
                    // guarantees that all requested invocations will run. Extra enqueues are OK because they will be no-ops
                    // from the perspective of the script.
                    scriptQueue.add(invocation);
                    continue;
                }

                try{
                    if(script.setStatus(ChipmunkScript.Status.RUNNING) == ChipmunkScript.Status.RUNNABLE){
                        script.run().ifPresent(o -> invocation.getFuture().complete(o));
                        var exited = script.getHazelVM().state() == HazelVM.State.EXITED;
                        script.setStatus(exited ? ChipmunkScript.Status.EXITED : ChipmunkScript.Status.RUNNABLE);
                        if(exited){
                            var exitHandler = script.exitHandler();
                            if(exitHandler != null){
                                exitHandler.accept(script);
                            }
                        }
                    }
                    // Do nothing if the script is already running in another runner.
                }catch(Throwable t){
                    var handler = script.errorHandler();
                    if(handler != null){
                        handler.accept(script, t);
                    }
                }finally{
                    invocations.remove(script.getId());
                }
            }else{
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

}

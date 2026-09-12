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

import chipmunk.ChipmunkException;
import chipmunk.binary.BinaryFormatException;
import chipmunk.runtime.*;
import chipmunk.vm.HeapOverflowError;
import chipmunk.vm.ModuleLoader;
import chipmunk.vm.Uncatchable;
import chipmunk.vm.hazel.invoke.Linker;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Hazel is the low-level VM. It holds the entire runtime state for a single script. Only one thread may access an instance
 * of a Hazel VM at a time, with the exception of specifically marked methods (such as getting status & yielding).
 */
public class HazelVM {

    public enum State {
        NEW, RUNNING, SUSPENDED, EXITED
    }

    protected State state = State.NEW;
    protected EntryPoint entryPoint = EntryPoint.DEFAULT;
    protected final Deque<Fiber> fibers = new ArrayDeque<>();
    protected final Map<String, ChipmunkModule> modules = new HashMap<>();
    protected final ModuleLoader moduleLoader;

    protected final MemoryStats memoryStats;
    protected final Heap heap;

    protected Fiber currentFiber;
    protected Fiber lastFiber;

    protected final Linker linker;

    protected volatile boolean yieldRequested;

    public HazelVM(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
        memoryStats = new MemoryStats();
        heap = new Heap(this);
        heap.allocate(); // Allocate once to reserve the null pointer so that "real" allocations never result in null.
        linker = new Linker();
    }

    public Optional<Object> run(){
        try{
            if(state == State.NEW) {
                var module = (CModule) getModule(entryPoint.module());
                if(module == null){
                    throw new IllegalStateException("Entry point module " + entryPoint.module() + " not found");
                }
            }

            if(state == State.NEW || state == State.EXITED) {
                var module = (CModule) getModule(entryPoint.module());
                if(module == null){
                    throw new IllegalStateException("Entry point module " + entryPoint.module() + " not found");
                }

                var main = module.getMethod(entryPoint.method());
                if(main == null){
                    throw new IllegalStateException("Entry point method " + entryPoint.method() + " not found");
                }
                /*System.out.println("===== Method: " + main.name() + " =====");
                System.out.println(main.dumpCode());*/
                spawnFiber(main);
            }

            state = State.RUNNING;

            // As long as we have fibers in the queue, run until a fiber yields. Optional.empty() is returned until
            // the last fiber exits, at which point the result is boxed and returned.
            currentFiber = nextFiber();
            while (currentFiber != null) {
                runFiber(currentFiber);
                lastFiber = currentFiber;
                if(lastFiber.state() == Fiber.State.RUNNABLE && checkAndClearYield()){
                    state = State.SUSPENDED;
                    return Optional.empty(); // This fiber yielded due to an external request
                }
                currentFiber = nextFiber();
            }

            state = State.EXITED;

            // Return empty when yielded, return value of last fiber when normal exit happens.
            var value = lastFiber.lastReturned();
            return Optional.ofNullable(toHostValue(value));

        } catch (Throwable t) {
            throw t;
        }
    }

    private Fiber nextFiber() {
        var it = fibers.iterator();
        while (it.hasNext()) {
            var fiber = it.next();
            if (fiber.state() == Fiber.State.RUNNABLE) {
                it.remove();
                return fiber;
            }
        }
        return null;
    }

    public MemoryStats memoryStats() {
        return memoryStats;
    }

    public Heap heap(){
        return heap;
    }

    private void enqueue(Fiber fiber){
        fibers.add(fiber);
    }

    public Stream<Fiber> allFibers(){
        return Stream.concat(
                Stream.ofNullable(currentFiber),
                fibers.stream()
        );
    }

    public Stream<CModule> allCModules(){
        return modules.values().stream()
                .filter(chipmunkModule -> chipmunkModule instanceof CModule)
                .map(chipmunkModule -> (CModule) chipmunkModule);
    }

    protected Fiber spawnFiber(CMethod method, double... args){
        var fiber = new Fiber(this, method);
        fiber.stack[0] = method.module().selfPtr();
        for(int i = 0; i < args.length; i++){
            fiber.stack[i + 1] = args[i];
        }
        fiber.pushCallFrame(method, 0);
        enqueue(fiber);
        return fiber;
    }

    /**
     * Dispatch loop uses the "Nostradamus Distributor" pattern. The technical motivation and explanation for this
     * pattern is available at:
     * <a href="http://www.emulators.com/docs/nx25_nostradamus.htm">...</a>
     * <p>
     * In some benchmarks this will not have much (if any) impact, but in others the speedup is dramatic. The highest
     * measured speedup was a factor of ~3x better than without it. This is supported by using a negative IP target for
     * "hot loop" branches. This allows the interpreter's dispatch loop  to mirror (from the perspective of the CPU
     * branch predictor) the HVM instructions, allowing the branch predictor to correctly predict which HVM instruction
     * will be dispatched next.
     */
    protected void runFiber(Fiber fiber){
        while(!checkAndClearYield() && !fiber.completed()){
            var frame = fiber.currentFrame();
            var ip = frame.ip;
            var bp = frame.bp;

            if(frame.continuation != null) {
                frame.continuation.resume(fiber, frame);
                continue;
            }

            var code = frame.method.code();
            /*System.out.println("======================");
            for(int i = 0; i < code.length; i++){
                System.out.println(i + ": " + code[i]);
            }
            System.out.println("======================");
            System.out.println("Executing " + frame.method.name() + " IP: " + ip + " BP: " + bp);
            System.out.println(dumpStack(fiber, bp, code[Math.abs(ip)].sp));*/

            while(Math.abs(ip) < code.length){
                // Function calls, returns, loops, etc. will all cause this to be hit frequently.
                if(checkYield()){
                    ip = Math.abs(ip);
                    frame.ip = ip;
                    break;
                }
                try {
                    ip = Math.abs(ip);
                    var op = code[ip];
                    //System.out.println(op.toString() + " IP: " + ip + " BP: " + bp + " SP: " + op.sp + ": " + dumpStack(fiber, bp, code[Math.abs(ip)].sp));
                    ip = op.apply(fiber, ip, bp);
                    if(ip >= 0){
                        op = code[ip];
                        ip = op.apply(fiber, ip, bp);
                        if(ip >= 0){
                            op = code[ip];
                            ip = op.apply(fiber, ip, bp);
                            if(ip >= 0){
                                op = code[ip];
                                ip = op.apply(fiber, ip, bp);
                                if(ip >= 0){
                                    op = code[ip];
                                    ip = op.apply(fiber, ip, bp);
                                    if(ip >= 0){
                                        op = code[ip];
                                        ip = op.apply(fiber, ip, bp);
                                        if(ip >= 0){
                                            op = code[ip];
                                            ip = op.apply(fiber, ip, bp);
                                            if(ip >= 0){
                                                op = code[ip];
                                                ip = op.apply(fiber, ip, bp);
                                                if(ip >= 0){
                                                    op = code[ip];
                                                    ip = op.apply(fiber, ip, bp);
                                                    if(ip >= 0){
                                                        op = code[ip];
                                                        ip = op.apply(fiber, ip, bp);
                                                        if(ip >= 0){
                                                            op = code[ip];
                                                            ip = op.apply(fiber, ip, bp);
                                                            if(ip >= 0){
                                                                op = code[ip];
                                                                ip = op.apply(fiber, ip, bp);
                                                                if(ip >= 0){
                                                                    op = code[ip];
                                                                    ip = op.apply(fiber, ip, bp);
                                                                    if(ip >= 0){
                                                                        op = code[ip];
                                                                        ip = op.apply(fiber, ip, bp);
                                                                        if(ip >= 0){
                                                                            op = code[ip];
                                                                            ip = op.apply(fiber, ip, bp);
                                                                            if(ip >= 0){
                                                                                op = code[ip];
                                                                                ip = op.apply(fiber, ip, bp);
                                                                                if(ip >= 0){
                                                                                    op = code[ip];
                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                    if(ip >= 0){
                                                                                        op = code[ip];
                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                        if(ip >= 0){
                                                                                            op = code[ip];
                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                            if(ip >= 0){
                                                                                                op = code[ip];
                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                if(ip >= 0){
                                                                                                    op = code[ip];
                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                    if(ip >= 0){
                                                                                                        op = code[ip];
                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                        if(ip >= 0){
                                                                                                            op = code[ip];
                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                            if(ip >= 0){
                                                                                                                op = code[ip];
                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                if(ip >= 0){
                                                                                                                    op = code[ip];
                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                    if(ip >= 0){
                                                                                                                        op = code[ip];
                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                        if(ip >= 0){
                                                                                                                            op = code[ip];
                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                            if(ip >= 0){
                                                                                                                                op = code[ip];
                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                if(ip >= 0){
                                                                                                                                    op = code[ip];
                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                    if(ip >= 0){
                                                                                                                                        op = code[ip];
                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                        if(ip >= 0){
                                                                                                                                            op = code[ip];
                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                            if(ip >= 0){
                                                                                                                                                op = code[ip];
                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                if(ip >= 0){
                                                                                                                                                    op = code[ip];
                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                    if(ip >= 0){
                                                                                                                                                        op = code[ip];
                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                        if(ip >= 0){
                                                                                                                                                            op = code[ip];
                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                op = code[ip];
                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                    op = code[ip];
                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                        op = code[ip];
                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                            op = code[ip];
                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                                    if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                                        op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                                        ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                                        if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                                            op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                                            ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                                            if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                                                op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                                                ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                                                if(ip >= 0){
                                                                                                                                                                                                                                                                                                                                                                                                                                    op = code[ip];
                                                                                                                                                                                                                                                                                                                                                                                                                                    ip = op.apply(fiber, ip, bp);
                                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                }
                                                                                                                                                                                                            }
                                                                                                                                                                                                        }
                                                                                                                                                                                                    }
                                                                                                                                                                                                }
                                                                                                                                                                                            }
                                                                                                                                                                                        }
                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }catch(Throwable t){
                    if(t instanceof Uncatchable){
                        throw t;
                    }else if(t instanceof ChipmunkException e){
                        e.populateStackTrace();
                    }
                    var handled = false;
                    for(var block : frame.method.exceptionTable()){
                        if(block.beginIp() <= ip && ip < block.endIp()){
                            var ptr = fiber.vm().heap().allocateAndWrite(t);
                            if(ptr == Value.NULL_PTR_VALUE){
                                throw new HeapOverflowError(fiber, "Heap overflow occured while handling exception " + t);
                            }
                            fiber.stack[bp + block.exceptionLocalIndex()] = ptr;
                            ip = block.endIp();
                            handled = true;
                        }
                    }
                    if(!handled){
                        throw t;
                    }
                }
            }
        }

        if(!fiber.completed()){
            enqueue(fiber);
        }else if(fiber.isBlocking()){
            fiber.unblock();
        }

    }

    public final int invokeMethod(CMethod method, Fiber fiber, int ip, int bp, int sp){
        var callingFrame = fiber.currentFrame();
        //System.out.println("Calling frame before invoking " + method.name() + ": " + fiber.vm().dumpStack(fiber, bp, 5));
        callingFrame.ip = ip + 1; // Resume at next instruction following this one
        fiber.pushCallFrame(method, bp + sp - method.argCount());
        // This causes the interpreter to transfer control to the outer interpreter loop, where it will reset ip & bp
        // and transfer control to the newly called method.
        return Fiber.RETURN_SIGNAL;
    }

    protected double[] frameState(Fiber fiber, int bp, int sp){
        var copy = new double[bp + sp];
        System.arraycopy(fiber.stack, bp, copy, 0, bp + sp);
        return copy;
    }

    public String dumpStack(Fiber fiber, int bp, int sp){
        var frameState = frameState(fiber, bp, sp);
        var builder = new StringBuilder();
        builder.append('[');
        for(int i = 0; i < frameState.length; i++){
            if(Value.isPointer(frameState[i])){
                builder.append(Value.pointerToString(frameState[i]));
            }else{
                builder.append(frameState[i]);
            }
            if(i < frameState.length - 1){
                builder.append(", ");
            }
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * Request a preemptive yield. This can be called by any thread.
     */
    public void yield(){
        yieldRequested = true;
    }

    /**
     * Check if a yield has been requested already or not. This can be called by any thread.
     * @return true if yield requested (and has not yet occured), false otherwise.
     */
    public boolean isYieldRequested(){
        return yieldRequested;
    }

    public ModuleLoader moduleLoader(){
        return moduleLoader;
    }

    public ChipmunkModule getModule(String name){
        try {
            var module = modules.get(name);
            if(module == null){
                module = moduleLoader.load(name, bin -> new BinaryLoader(linker).loadModule(heap, bin));
                if(module instanceof NativeModule nModule){
                    nModule.registerTypeBindings(linker.binding());
                }
            }

            if(module == null){
                throw new RuntimeException("Module " + name + " not found");
            }

            modules.put(name, module);

            if(module instanceof CModule cModule){
                var ptr = heap.allocateAndWrite(cModule);
                cModule.selfPtr(ptr);

                // Note: All sorts of funkiness can happen with import cycles between modules. That won't cause a runtime
                // error in and of itself, but may result in null errors because module values being read before their
                // initializer runs. This is intentional.

                // Do imports before this module initializer runs so that their initializers get queued ahead of ours
                // if they're not already initialized.
                // TODO - this won't work if yields happen while initializers are running.
                for(var imp : cModule.imports()){
                    var field = cModule.getField("$" + imp.name().replace('.', '_'));
                    var impModule = getModule(imp.name());
                    cModule.getFields()[field] = switch (impModule){
                        case CModule impCModule -> impCModule.selfPtr();
                        default -> heap.allocateAndWrite(impModule);
                    };
                }

                var init = cModule.getMethod("$module_init$");
                if(init != null && !cModule.isInitialized()){
                    cModule.markInitialized();
                    var initFiber = spawnFiber(init, heap.allocateAndWrite(this));
                    if(currentFiber != null){
                        initFiber.block(currentFiber);
                        this.yield();
                    }
                }
            }
            return module;
        } catch (IOException | BinaryFormatException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public void entryPoint(EntryPoint entryPoint){
        this.entryPoint = entryPoint;
    }

    public EntryPoint entryPoint(){
        return entryPoint;
    }

    public Linker linker(){
        return linker;
    }

    public Object toHostValue(double v){
        if(Value.isPointer(v)){
            if(Value.NULL_PTR_VALUE == v){
                return null;
            }else{
                return heap.read(v);
            }
        }else{
            return v;
        }
    }

    public double fromHostValue(Object v){
        return switch (v){
            case Double d -> d;
            case null -> Value.NULL_PTR_VALUE;
            case HostCObject cObj -> {
                if(!Value.isNullPointer(cObj.selfPtr())){
                    yield cObj.selfPtr();
                }
                var ptr = heap.allocateAndWrite(cObj);
                cObj.selfPtr(ptr);
                yield ptr;
            }
            default -> heap.allocateAndWrite(v);
        };
    }

    private boolean checkYield(){
        return yieldRequested;
    }

    private boolean checkAndClearYield(){
        if(yieldRequested){
            yieldRequested = false;
            return true;
        }
        return false;
    }

    public String typeName(double ptr){
        return typeName(heap.read(ptr));
    }

    public String typeName(Object t){
        return switch (t){
            case null -> "null";
            case CObject ins -> ((CClass) heap.read(ins.storage()[0])).name();
            case CClass c -> c.name();
            case CModule m -> m.name();
            default -> t.getClass().getName();
        };
    }
}

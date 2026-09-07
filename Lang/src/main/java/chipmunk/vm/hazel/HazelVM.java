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

import chipmunk.ChipmunkRuntimeException;
import chipmunk.binary.BinaryFormatException;
import chipmunk.runtime.CMethod;
import chipmunk.runtime.CModule;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.ModuleLoader;

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
    protected final GarbageCollector gc;

    protected Fiber currentFiber;
    protected Fiber lastFiber;

    protected volatile boolean yieldRequested;

    public HazelVM(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
        memoryStats = new MemoryStats();
        heap = new Heap();
        heap.allocate(); // Allocate once to reserve the null pointer so that "real" allocations never result in null.
        // TODO - support GC pinning, and pin this so that the GC can never free the null pointer and allow it to be used.
        gc = new GarbageCollector(this, heap);
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

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    protected Fiber spawnFiber(CMethod method){
        var fiber = new Fiber(this, method);
        fiber.stack[0] = method.module().selfPtr();
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
                    //throw t; // TODO
                    t.printStackTrace();
                    throw new ChipmunkRuntimeException(t.getMessage());
                }
            }
        }

        if(!fiber.completed()){
            enqueue(fiber);
        }else if(fiber.isBlocking()){
            fiber.unblock();
        }

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

    public void yield(){
        yieldRequested = true;
    }

    public boolean isYieldRequested(){
        return yieldRequested;
    }

    public ModuleLoader moduleLoader(){
        return moduleLoader;
    }

    public ChipmunkModule getModule(String name){
        try {
            CModule module = (CModule) modules.get(name);
            if(module == null){
                module = (CModule) moduleLoader.load(name, BinaryLoader::loadModule);
            }
            if(module == null){
                throw new RuntimeException("Module " + name + " not found");
            }
            var ptr = heap.allocateAndWrite(module);
            module.selfPtr(ptr);
            modules.put(module.getName(), module);

            var init = module.getMethod("$module_init$");
            if(init != null && !module.isInitialized()){
                module.markInitialized();
                var initFiber = spawnFiber(init);
                if(currentFiber != null){
                    initFiber.block(currentFiber);
                    this.yield();
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
}

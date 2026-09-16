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

import chipmunk.vm.hazel.util.BitFieldAllocator;

public final class Heap {

    public static final int ALLOC_FAILURE = -1;

    public static final int DEFAULT_INITIAL_HEAP_SIZE = 1024;
    public static final int DEFAULT_GROWTH_STEP = 1024;
    public static final int DEFAULT_HEAP_LIMIT = 2048;

    private final int step;
    private Object[] memory;
    private final BitFieldAllocator allocator;

    private final HazelVM vm;
    private final GarbageCollector gc;

    public Heap(HazelVM vm){
        this(vm, DEFAULT_INITIAL_HEAP_SIZE);
    }

    public Heap(HazelVM vm, int initialHeapSize){
        this(vm, initialHeapSize, DEFAULT_GROWTH_STEP);
    }

    public Heap(HazelVM vm, int initialHeapSize, int step){
        if(!isValidHeapSize(initialHeapSize)){
            throw new IllegalArgumentException("Invalid heap size, must be a multiple of 64: " + initialHeapSize);
        }
        if(!isValidHeapSize(step)){
            throw new IllegalArgumentException("Invalid heap step, must be a multiple of 64: " + step);
        }
        this.vm = vm;
        this.step = step;
        memory = new Object[initialHeapSize];
        allocator = new BitFieldAllocator(initialHeapSize);
        gc = new GarbageCollector(vm, this);
        // TODO - support GC pinning, and pin this so that the GC can never free the null pointer and allow it to be used.
        allocate(); // Allocate once to reserve the null pointer so that "real" allocations never result in null.
    }

    public Object read(double ptr){
        return read(Value.getPointer(ptr));
    }

    public Object read(int ptr){
        return memory[ptr];
    }

    public void write(int ptr, Object value){
        try{
            memory[ptr] = value;
        }catch(ArrayIndexOutOfBoundsException e){
            growHeap(ptr);
            memory[ptr] = value;
        }
    }

    public BitFieldAllocator allocator(){
        return allocator;
    }

    public GarbageCollector gc(){
        return gc;
    }

    public int freeSpace(){
        return allocator.freeSpace();
    }

    public double allocateAndWrite(Object obj){
        var ptr = allocate();
        write(ptr, obj);
        return Value.makePointer(ptr);
    }

    public int allocate(){
        var ptr = allocator.allocate();
        if(ptr == ALLOC_FAILURE){
            gc.collect();
            ptr = allocator.allocate();
            if(ptr == ALLOC_FAILURE){
                throw new HeapOverflowException(Value.NULL_POINTER, "Heap is full");
            }
        }
        if(ptr >= memory.length){
            growHeap(ptr);
        }
        return ptr;
    }

    public void free(int ptr){
        memory[ptr] = null;
        allocator.free(ptr);
    }

    private void growHeap(int outOfBoundsPtr){
        try{
            var limit = vm.limits().heapSlots();
            if(outOfBoundsPtr >= limit){
                throw new HeapOverflowException(outOfBoundsPtr, "Required new heap size would exceed array size limits");
            }
            var tmp = new Object[Math.toIntExact(Math.min(limit, memory.length + outOfBoundsPtr + step))];
            System.arraycopy(memory, 0, tmp, 0, memory.length);
            memory = tmp;
            allocator.resize(memory.length);
        } catch (ArithmeticException ex) {
            throw new HeapOverflowException(outOfBoundsPtr, "Required new heap size would exceed array size limits");
        }
    }

    public static boolean isValidHeapSize(int size){
        return size % 64 == 0;
    }
}

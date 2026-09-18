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

public class Limits {

    protected int callStackDepth;
    protected int stackStorage;
    protected int heapSlots;
    protected int fiberCount;

    public Limits() {
        heapSlots = Heap.DEFAULT_HEAP_LIMIT;
        callStackDepth = Fiber.DEFAULT_CALL_FRAMES_LIMIT;
        stackStorage = Fiber.DEFAULT_STACK_LIMIT;
        fiberCount = HazelVM.DEFAULT_FIBER_COUNT_LIMIT;
    }

    public Limits(int heapSlots, int callStackDepth, int stackStorage, int fiberCount) {
        this.heapSlots = heapSlots;
        this.callStackDepth = callStackDepth;
        this.stackStorage = stackStorage;
        this.fiberCount = fiberCount;

        if(!Heap.isValidHeapSize(heapSlots)){
            throw new IllegalArgumentException("Invalid heap size, must be a multiple of 64: " + heapSlots);
        }
    }

    public int callStackDepth() {
        return callStackDepth;
    }

    public void callStackDepth(int callStackDepth) {
        if(callStackDepth < this.callStackDepth) {
            throw new IllegalArgumentException("callStackDepth must be >= current callStackDepth");
        }
        this.callStackDepth = callStackDepth;
    }

    public int stackStorage() {
        return stackStorage;
    }

    public void stackStorage(int stackStorage) {
        if(stackStorage < this.stackStorage) {
            throw new IllegalArgumentException("stackStorage must be >= current stackStorage");
        }
        this.stackStorage = stackStorage;
    }

    public int heapSlots() {
        return heapSlots;
    }

    public void heapSlots(int heapSlots) {
        if(heapSlots < this.heapSlots) {
            throw new IllegalArgumentException("heapSlots must be >= current heapSlots");
        }
        this.heapSlots = heapSlots;
    }

    public int fiberCount() {
        return fiberCount;
    }

    public void fiberCount(int fiberCount) {
        if(fiberCount < this.fiberCount) {
            throw new IllegalArgumentException("fiberCount must be >= current fiberCount");
        }
        this.fiberCount = fiberCount;
    }

    public boolean canSpawn(int currentFibers){
        return currentFibers + 1 <= fiberCount;
    }

    public void copyFrom(Limits other){
        this.callStackDepth = other.callStackDepth;
        this.stackStorage = other.stackStorage;
        this.heapSlots = other.heapSlots;
        this.fiberCount = other.fiberCount;
    }
}

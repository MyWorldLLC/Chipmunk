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

public final class MemoryStats {

    public static final int BYTES_PER_SLOT = 8;

    private int estimatedReachable;
    private int heapSize;
    private int fiberStacks;
    private int fiberFrames;

    public void instanceCreated(int slots){
        bumpEstimatedReachable(slots * BYTES_PER_SLOT);
    }

    public void instanceFreed(int slots){
        bumpEstimatedReachable(-slots * BYTES_PER_SLOT);
    }

    public void bumpEstimatedReachable(int delta){
        estimatedReachable += delta;
    }

    public int estimatedReachable(){
        return estimatedReachable;
    }

    public int heapSize(){
        return heapSize;
    }

    public void heapSize(int slots){
        heapSize = slots * BYTES_PER_SLOT;
    }

    public int fiberStacks(){
        return fiberStacks;
    }

    public void fiberStacks(int totalSlots, int totalFrames){
        fiberStacks = totalSlots * BYTES_PER_SLOT;
        fiberFrames = totalFrames * Fiber.Frame.FRAME_SIZE;
    }

    public void resetUsage(){
        estimatedReachable = 0;
        heapSize = 0;
        fiberStacks = 0;
        fiberFrames = 0;
    }

    public int totalUsage(){
        return estimatedReachable + heapSize + fiberStacks + fiberFrames;
    }

}

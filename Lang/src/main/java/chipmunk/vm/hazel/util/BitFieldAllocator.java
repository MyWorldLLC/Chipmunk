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

package chipmunk.vm.hazel.util;

/**
 * Allocator that uses a bitfield to track the state of the resource. This doesn't require any access
 * to the resource being allocated from. This is meant to be used to track free array indices, so only single
 * element allocations are supported.
 */
public class BitFieldAllocator {

    public static final int ALLOC_FAILURE = -1;

    private final BitField state;
    private int lastFree = 0;
    private int allocated = 0;

    public BitFieldAllocator(int initialSize){
        state = new BitField(initialSize);
    }

    public int allocate(){
        if(lastFree != -1){
            state.set(lastFree);
            var ptr = lastFree;
            lastFree = -1;
            allocated++;
            return ptr;
        }

        for(int i = 0; i < state.wordCount(); i++){
            var word = state.word(i);
            var freeBit = Long.highestOneBit(~word);
            if(freeBit != 0){
                var ptr = i * 64 + Long.numberOfLeadingZeros(freeBit);
                state.set(ptr);
                allocated++;
                return ptr;
            }
        }
        return ALLOC_FAILURE;
    }

    public boolean isAllocated(int ptr){
        return state.isSet(ptr);
    }

    public void free(int ptr){
        allocated--;
        state.clear(ptr);
        lastFree = ptr;
    }

    public int bitCount(){
        return state.bitCount();
    }

    public int freeSpace(){
        return state.bitCount() - allocated;
    }

    public void resize(int newSize){
        state.resize(newSize);
    }

}


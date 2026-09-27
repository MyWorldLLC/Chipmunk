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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static chipmunk.vm.hazel.util.BitFieldAllocator.ALLOC_FAILURE;
import static org.junit.jupiter.api.Assertions.*;

public class BitFieldAllocatorTest {

    private BitFieldAllocator allocator;

    @BeforeEach
    public void setupBitField(){
        allocator = new BitFieldAllocator(128);
    }

    @Test
    public void allocateCheckAndFree(){
        var ptr = allocator.allocate();
        assertNotEquals(ALLOC_FAILURE, ptr, "Allocation failed");

        assertTrue(allocator.isAllocated(ptr), "Allocator reports pointer is not allocated");

        allocator.free(ptr);

        assertFalse(allocator.isAllocated(ptr), "Allocator reports pointer is allocated after free");
    }

    @Test
    public void allocationFailure(){
        var allocator = new BitFieldAllocator(64);
        for(int i = 0; i < 64; i++){
            allocator.allocate();
        }
        var ptr = allocator.allocate();
        assertEquals(ALLOC_FAILURE, ptr, "Allocator claims success when it did not have capacity for allocation");
    }

    @Test
    public void repeatedAllocations(){
        var allocator = new BitFieldAllocator(64);
        for(int i = 0; i < 64; i++){
            var ptr = allocator.allocate();
            assertNotEquals(ALLOC_FAILURE, ptr);
        }
    }
}

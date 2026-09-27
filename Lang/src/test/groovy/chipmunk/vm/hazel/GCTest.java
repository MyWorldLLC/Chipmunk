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

import chipmunk.runtime.CField;
import chipmunk.runtime.CModule;
import chipmunk.runtime.CObject;
import chipmunk.vm.ModuleLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GCTest {

    private HazelVM vm;
    private Heap heap;

    @BeforeEach
    public void setupHeap(){
        vm = new HazelVM(new ModuleLoader(), new Limits(64, 10, 10, 10));
        heap = vm.heap();
    }

    @Test
    public void initialHeapFullyFree() {
        assertEquals(63, heap.freeSpace());
    }

    @Test
    public void collectWhenFull(){
        for(int i = 0; i < 126; i++){
            heap.allocate();
        }
        // Note that null pointer takes up a heap slot, even though it can never be freed.
        assertEquals(0, heap.freeSpace());
        assertEquals(63, heap.gc().collectionStats().reclaimedSlots());
    }

    @Test
    public void fillHeapAndFailWithInterleavedGC(){
        var module = new CModule("test", "test.chp");
        module.setFields(new CField[]{new CField("test", 0)});
        module.selfPtr(heap.allocateAndWrite(module));
        vm.modules.put("test", module);

        var obj = new CObject(new double[61]);
        obj.selfPtr(heap.allocateAndWrite(obj));
        module.getFields()[0] = obj.selfPtr();

        for(int i = 0; i < 61; i++){
            obj.storage()[i] = Value.makePointer(heap.allocate());
            if(i % 4 == 0){
                heap.gc().collect();
            }
        }

        assertThrows(HeapOverflowException.class, () -> heap.allocate());
    }
}

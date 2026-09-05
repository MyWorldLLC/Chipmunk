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

import chipmunk.vm.hazel.util.BitField;

public class GarbageCollector {

    protected final HazelVM vm;
    protected final Heap heap;

    public GarbageCollector(HazelVM vm, Heap heap) {
        this.vm = vm;
        this.heap = heap;
    }

    public void collect(){
        // Tri-color mark & sweep. All objects are initially assumed to be in the white set (reclaimable).
        // Initialize the black set (known reachable) by scanning all fiber stacks & module variables. As each reference is
        // marked in the black set, mark any references reachable via that reference in the grey set. Once the grey set is
        // empty, traverse the allocated pointers and remove any that are not  marked in the black set. For partial collections,
        // full reachability must be established but the entire heap doesn't need to be traversed and cleared at once as
        // long as new allocations happen only in an already-collected section of the heap. The white set can be discovered
        // once and re-used until an entire collection cycle completes, because once a reference is in the white set
        // it will never be reachable again.

        var black = new BitField(heap.allocator().bitCount());
        // Note: we should probably use a queue rather than a bitfield for efficiently tracking the grey set, but for
        // now the bitfield has much better space efficiency.
        var grey = new BitField(heap.allocator().bitCount());

        var allocator = heap.allocator();

        // Mark initial black set
        vm.allCModules().forEach(module -> {
            var fields = module.getFields();
            for(var field : fields){
                markIfPointer(black, grey, field);
            }
        });

        vm.allFibers().forEach(fiber-> {
            var stack = fiber.stack();
            var sp = fiber.currentFrame().sp;
            for(int i = 0; i < sp; i++){
                markIfPointer(black, grey, stack[i]);
            }
        });

        // Scan the grey set until the grey set is empty
        var greyEmpty = false;
        while(!greyEmpty){
            var foundOrMarkedPointer = false;
            for(int i = 0; i < grey.bitCount(); i++){
                if(grey.isSet(i)){
                    foundOrMarkedPointer = true;
                    // We're exploring it, so move from grey set to black set.
                    grey.clear(i);
                    black.set(i);
                    markFields(black, grey, heap.read(i));
                }
            }
            greyEmpty = !foundOrMarkedPointer;
        }


        // Once the grey set is empty, negating the black set gives the white set.
        black.negate();
        for(int i = 0; i < black.bitCount(); i++){
            if(black.isSet(i)){
                allocator.free(i);
                // TODO - estimate and record overall memory impact of freeing these objects
            }
        }
    }

    private void markIfPointer(BitField black, BitField grey, double v){
        if(Value.isPointer(v)){
            var ptr = (int) Value.getPointer(v);
            if(ptr != 0){
                black.set(ptr);
                var obj = heap.read(ptr);
                markFields(black, grey, obj);
            }
        }
    }

    private void markFields(BitField black, BitField grey, Object obj){
        if(obj instanceof double[] d){
            for(int i = 0; i < d.length; i++){
                var v = d[i];
                if(Value.isPointer(v)){
                    var ptr = (int) Value.getPointer(v);
                    if(!black.isSet(ptr)){
                        grey.set(ptr);
                    }
                }
            }
        }
        // If this isn't a double[] then we know that it's a native object and we
        // have nothing to do.
    }

}

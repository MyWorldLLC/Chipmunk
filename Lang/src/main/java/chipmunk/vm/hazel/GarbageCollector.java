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

    public static class GCCollection {
        protected final GarbageCollector collector;
        protected final BitField black;
        protected final BitField grey;

        protected GCCollection(GarbageCollector collector, BitField black, BitField grey) {
            this.collector = collector;
            this.black = black;
            this.grey = grey;
        }

        public void visit(double ptr){
            collector.markIfPointer(this, ptr);
        }

        public void visitStorage(double[] storage){
            collector.visitStorage(this, storage);
        }
    }

    protected final HazelVM vm;
    protected final Heap heap;
    protected final CollectionStats stats;

    public GarbageCollector(HazelVM vm, Heap heap) {
        this.vm = vm;
        this.heap = heap;
        stats = new CollectionStats();
    }

    public CollectionStats collectionStats() {
        return stats;
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

        stats.reset();

        var black = new BitField(heap.allocator().bitCount());
        // Note: we should probably use a queue rather than a bitfield for efficiently tracking the grey set, but for
        // now the bitfield has much better space efficiency.
        var grey = new BitField(heap.allocator().bitCount());

        var collection = new GCCollection(this, black, grey);

        var allocator = heap.allocator();

        black.set(0); // Null pointer must always be in the black set since we don't have pinning yet.

        // Mark modules and fiber stacks
        vm.allCModules().forEach(module -> {
            collection.grey.set(Value.getPointer(module.selfPtr()));
        });

        vm.allFibers().forEach(fiber-> {
            var stack = fiber.stack();
            var frame = fiber.currentFrame();
            var stackDepth = frame.bp + frame.method.maxStack();
            for(int i = 0; i < frame.bp + stackDepth; i++){
                markIfPointer(collection, stack[i]);
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
                    markFields(collection, heap.read(i));
                }
            }
            greyEmpty = !foundOrMarkedPointer;
        }


        // Once the grey set is empty, negating the black set gives the set of all unvisited pointers.
        // The white set is the intersection of all unvisited pointers with the allocated pointers.
        black.negate();
        black.intersect(allocator.allocated());
        for(int i = 0; i < black.bitCount(); i++){
            if(black.isSet(i)){
                heap.free(i);
                // TODO - estimate and record overall memory impact of freeing these objects
                stats.slotFreed();
            }
        }
    }

    private void markIfPointer(GCCollection collection, double v){
        if(Value.isPointer(v)){
            var ptr = Value.getPointer(v);
            if(ptr != 0){
                collection.grey.set(ptr);
                var obj = heap.read(ptr);
                markFields(collection, obj);
            }
        }
    }

    private void markFields(GCCollection collection, Object obj){
        if(obj instanceof GCCollectable collectable){
            collectable.gcVisit(collection);
        }
        // If this isn't a GCCollectable then we know that it's a native object that does not expose storage to
        // the GC and we have nothing to do.
    }

    protected void visitStorage(GCCollection collection, double[] storage){
        for(var p : storage){
            if(Value.isPointer(p)){
                var ptr = Value.getPointer(p);
                if(ptr != Value.NULL_POINTER){
                    if(!collection.black.isSet(ptr)){
                        collection.grey.set(ptr);
                    }
                }
            }
        }
    }

}

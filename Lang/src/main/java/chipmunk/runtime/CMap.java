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

package chipmunk.runtime;

import chipmunk.vm.hazel.GCCollectable;
import chipmunk.vm.hazel.GarbageCollector;
import chipmunk.vm.hazel.HazelVM;
import chipmunk.vm.hazel.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CMap extends HostCObject implements GCCollectable {

    public static final int DEFAULT_INITIAL_SIZE = 10;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    protected final HazelVM vm;
    private final float loadFactor;
    private int count;
    private int slots;
    private int nominalSize;
    protected double[] storage;

    public CMap(HazelVM vm) {
        this(vm, DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public CMap(HazelVM vm, int initialSize) {
        this(vm, initialSize, DEFAULT_LOAD_FACTOR);
    }

    public CMap(HazelVM vm, int initialSize, float loadFactor) {
        if(loadFactor <= 0.0 || loadFactor >= 1.0f){
            throw new IllegalArgumentException("Load factor must be in the range (0.0, 1.0)");
        }

        this.vm = vm;

        this.loadFactor = loadFactor;
        nominalSize = initialSize;
        slots = Math.max(1, (int) (initialSize / loadFactor));

        vm.memoryStats().instanceCreated(slots * 2); // TODO - error if we go above limits and GC can't claim enough to satisfy our request
        storage = new double[slots * 2];
        Arrays.fill(storage, Value.NULL_PTR_VALUE);
    }

    public double insert(double k, double v){
        if(Value.isNullPointer(k)){
            throw new IllegalArgumentException("Map keys may not be null");
        }
        if(count + 1 >= nominalSize){
            growTable();
        }

        var index = keyIndex(k, true);
        if(index >= 0){
            var eKey = storage[index];
            storage[index] = k;
            var prior = storage[index + 1];
            storage[index + 1] = v;
            if(Value.isNullPointer(eKey)){
                count++;
            }
            return prior;
        }

        // We didn't find a slot, so grow the table and try again. There are guaranteed to be free slots now.
        // This is incredibly unlikely to occur since we already check the load factor and grow the table
        // as needed before insert, but this fallback provides robustness against strange load factors.
        growTable();
        return insert(k, v);
    }

    public double get(double k){
        if(Value.isNullPointer(k)){
            throw new IllegalArgumentException("Map keys may not be null");
        }

        var index = keyIndex(k, false);
        if(index >= 0){
            return storage[index + 1];
        }
        return Value.NULL_PTR_VALUE;
    }

    public boolean contains(double k){
        return !Value.isNullPointer(get(k));
    }

    public double remove(double k){
        if(Value.isNullPointer(k)){
            throw new IllegalArgumentException("Map keys may not be null");
        }
        var index = keyIndex(k, false);
        if(index >= 0){
            storage[index] = Value.NULL_PTR_VALUE;
            var v = storage[index + 1];
            storage[index + 1] = Value.NULL_PTR_VALUE;
            count--;
            return v;
        }
        return Value.NULL_PTR_VALUE;
    }

    public void clear(){
        Arrays.fill(storage, Value.NULL_PTR_VALUE);
        count = 0;
    }

    public int size(){
        return count;
    }

    public int capacity(){
        return slots;
    }

    public void copyTo(CMap other){
        for(int i = 0; i < storage.length; i += 2){
            var k = storage[i];
            if(!Value.isNullPointer(k)){
                other.insert(k, storage[i + 1]);
            }
        }
    }

    public void compact(){
        var other = new CMap(vm, count, loadFactor);
        copyTo(other);
        nominalSize = count;
        slots = other.slots;
        storage = other.storage;
    }

    public double[] keys(){
        var keys = new double[count];
        for(int i = 0, j = 0; i < storage.length; i += 2){
            var k = storage[i];
            if(!Value.isNullPointer(k)){
                keys[j] = k;
                j++;
            }
        }
        return keys;
    }

    public double[] values(){
        var values = new double[count];
        for(int i = 0, j = 0; i < storage.length; i += 2){
            var k = storage[i];
            if(!Value.isNullPointer(k)){
                values[j] = storage[i + 1];
                j++;
            }
        }
        return values;
    }

    public Map<Object, Object> toMap(){
        var result = new HashMap<>();
        for(int i = 0; i < storage.length; i += 2){
            if(!Value.isNullPointer(storage[i])){
                result.put(vm.toHostValue(storage[i]), vm.toHostValue(storage[i + 1]));
            }
        }
        return result;
    }

    private void growTable(){
        // TODO - account for old table that we're eliminating so we don't run GC unnecessarily.
        vm.memoryStats().instanceCreated(slots * 2); // TODO - error if we go above limits and GC can't claim enough to satisfy our request

        var oldTable = storage;
        slots += (int) (0.5 * slots); // TODO - configurable growth factor
        nominalSize = (int) (slots * loadFactor);
        storage = new double[slots * 2];
        Arrays.fill(storage, Value.NULL_PTR_VALUE);

        if(oldTable != null){
            count = 0;
            // This works because we've already snagged a copy of the storage and set the count to 0
            for(int i = 0; i < oldTable.length; i += 2){
                var k = oldTable[i];
                if(!Value.isNullPointer(k)){
                    insert(k, oldTable[i + 1]);
                }
            }
        }
    }

    private int keyIndex(double k, boolean insertion){
        var hashedIndex = hashKey(k) % slots * 2; // Multiply by 2 since keys/values are interleaved
        for(int i = hashedIndex; i < storage.length; i += 2){
            var eKey = storage[i];
            if(eKey == k){
                return i;
            }else if(insertion && Value.isNullPointer(eKey)){
                return i;
            }else if(Value.isPointer(eKey) && Value.isPointer(k)){
                if(Value.getPointer(eKey) == Value.getPointer(k)){
                    return i;
                }else{
                    var k1 = vm.toHostValue(eKey);
                    var k2 = vm.toHostValue(k);
                    if(Objects.equals(k1, k2)){
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private int hashKey(double k){
        if(Value.isNumber(k)){
            return Double.hashCode(k);
        }else{
            var key = vm.toHostValue(k);
            return key == null ? 0 : key.hashCode();
        }
    }

    @Override
    public void gcVisit(GarbageCollector.GCCollection collection) {
        collection.visitStorage(storage);
    }
}

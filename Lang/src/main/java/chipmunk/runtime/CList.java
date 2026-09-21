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

import java.util.Arrays;
import java.util.List;

public class CList extends HostCObject implements GCCollectable {

    public static final int DEFAULT_CAPACITY = 10;

    protected final HazelVM vm;
    protected double[] storage;
    protected int insertIndex;

    public CList(HazelVM vm){
        this(vm, DEFAULT_CAPACITY);
    }

    public CList(HazelVM vm, int capacity){
        this.vm = vm;
        vm.memoryStats().instanceCreated(capacity); // TODO - error if we go above limits and GC can't claim enough to satisfy our request
        storage = new double[capacity];
    }

    public void add(double e){
        if(insertIndex == storage.length){
            var newStorage = new double[Math.max(1, storage.length * 2)];
            System.arraycopy(storage, 0, newStorage, 0, storage.length);
            storage = newStorage;
        }
        storage[insertIndex] = e;
        insertIndex++;
    }

    public double get(int i){
        i = normalizedIndex(i);
        return storage[i];
    }

    public double set(int i, double e){
        i = normalizedIndex(i);
        var prior = storage[i];
        storage[i] = e;
        return prior;
    }

    public double remove(int i){
        i = normalizedIndex(i);
        var prior = storage[i];
        System.arraycopy(storage, i + 1, storage, i, storage.length - i - 1);
        insertIndex--;
        return prior;
    }

    public boolean contains(double value){
        for(int i = 0; i < insertIndex; i++){
            if(storage[i] == value){
                return true;
            }
        }
        return false;
    }

    public void compact(){
        var newCapacity = storage.length - (storage.length - size());
        var newStorage = new double[newCapacity];
        System.arraycopy(storage, 0, newStorage, 0, newStorage.length);
        storage = newStorage;
    }

    public void clear(){
        Arrays.fill(storage, 0);
        insertIndex = 0;
    }

    public int size(){
        return insertIndex;
    }

    public void sort(CList keys){
        if(keys == null){
            sort();
            return;
        }else if(keys.size() != size()){
            throw new IllegalArgumentException("Key list size must match list size");
        }
        // Insertion sort. This is generally best for small lists, which is most likely the case for Chipmunk.
        for(int i = 1; i < size(); i++){
            var j = i;
            var key = keys.get(i);
            var e = storage[i];
            while(j > 0 && keys.get(j - 1) > key){
                keys.set(j, keys.get(j - 1));
                storage[j] = storage[j - 1];
                j--;
            }
            keys.set(j, key);
            storage[j] = e;
        }
    }

    public void sort(){
        Arrays.sort(storage);
    }

    public double[] rawStorage(){
        return storage;
    }

    public List<Double> toList(){
        return Arrays.stream(storage)
                .limit(size())
                .boxed()
                .toList();
    }

    public Object[] toHostArray(){
        return Arrays.stream(storage)
                .limit(size())
                .mapToObj(vm::toHostValue)
                .toArray(Object[]::new);
    }

    private int normalizedIndex(int index){
        return index >= 0 ? index : insertIndex - Math.abs(index);
    }

    @Override
    public void gcVisit(GarbageCollector.GCCollection collection) {
        collection.visitStorage(storage);
    }
}

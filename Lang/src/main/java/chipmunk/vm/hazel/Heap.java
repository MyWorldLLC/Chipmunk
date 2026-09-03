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

public class Heap {

    public static final int DEFAULT_INITIAL_HEAP_SIZE = 1024;
    public static final int DEFAULT_GROWTH_STEP = 1024;
    public static final int DEFAULT_HEAP_LIMIT = 2048;

    protected int limit;
    protected int step;
    protected Object[] memory;

    public Heap(){
        this(DEFAULT_INITIAL_HEAP_SIZE, DEFAULT_HEAP_LIMIT);
    }

    public Heap(int initialHeapSize){
        this(initialHeapSize, DEFAULT_HEAP_LIMIT);
    }

    public Heap(int initialHeapSize, int limit){
        this(initialHeapSize, limit, DEFAULT_GROWTH_STEP);
    }

    public Heap(int initialHeapSize, int limit, int step){
        this.limit = limit;
        this.step = step;
        memory = new Object[initialHeapSize];
    }

    public Object read(long ptr){
        return memory[(int) ptr];
    }

    public void write(long ptr, Object value){
        try{
            memory[(int) ptr] = value;
        }catch(ArrayIndexOutOfBoundsException e){
            try{
                if(ptr >= limit){
                    throw e; // TODO - customize & attach the bad pointer
                }
                var tmp = new Object[Math.toIntExact(Math.min(limit, memory.length + ptr + step))];
                System.arraycopy(memory, 0, tmp, 0, memory.length);
                memory = tmp;
            } catch (ArithmeticException ex) {
                throw new HeapOverflowException(ptr, "Required new heap size would exceed array size limits");
            }
        }
    }
}

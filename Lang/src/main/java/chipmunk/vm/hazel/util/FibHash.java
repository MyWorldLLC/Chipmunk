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
 * Fast 32-bit integer hashing based on https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/
 */
public class FibHash {

    public static int hash(int key){
        var fib = 1140071481932319848L;
        var hash = fib * key;
        hash ^= hash >> 32; // Mix in the high bits
        return (int) hash;
    }

    /**
     * @param hash hash code to map to the slot count range
     * @param slotCount must be a power of two
     */
    public static int hashToRange(int hash, int slotCount){
        return hash & (slotCount - 1);
    }

    public static int nextPowerOf2(int n){
        // Round up to nearest power of two
        var b = Integer.highestOneBit(n);
        return n > b ? b << 1 : b;
    }
}

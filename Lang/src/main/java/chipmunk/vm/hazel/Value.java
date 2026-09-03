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

/**
 * Utility class that implements NaN tagging for efficient representation of diverse values.
 * See <a href="https://anniecherkaev.com/the-secret-life-of-nan">the-secret-life-of-nan</a> and
 * <a href="https://wingolog.org/archives/2011/05/18/value-representation-in-javascript-implementations">value-representation-in-javascript-implementations</a>
 * for more information.
 */
public class Value {

    // The NaN mask matches the exponent & the first bit of the mantissa, skipping the sign
    // The type mask matches the 15 bits of the mantissa following the NaN mask.

    // Currently there are two fundamental types: numbers and pointers.

    // Note that this encoding leaves 44 bits to encode a pointer's space. Currently, HazelVM only addresses
    // up to 2G memory entries, but this leaves the option open for things like segmented memories to support a
    // larger address space.

    public static final long NAN_MASK          = 0x7FF8_0000_0000_0000L;
    public static final long TYPE_MASK         = 0x0007_F000_0000_0000L;
    public static final long POINTER_TYPE_FLAG = 0x0007_F000_0000_0000L;

    public static final long NULL_POINTER = 0L;

    private Value(){} // This is a static helper class, so should never be instantiated

    public static boolean isNumber(double v){
        return v == v; // This will be false for NaN
    }

    public static long toBits(double v){
        return Double.doubleToRawLongBits(v); // We have to use this to preserve NaN bit patterns
    }

    public static double fromBits(long v){
        return Double.longBitsToDouble(v);
    }

    public static boolean isPointer(double v){
        return isType(v, POINTER_TYPE_FLAG);
    }

    public static boolean isType(double v, long typeFlag){
        return (toBits(v) & TYPE_MASK) == typeFlag;
    }

    public static double makeValue(long v, long typeFlag){
        return fromBits(v | typeFlag | NAN_MASK);
    }

    public static double makePointer(long address){
        return makeValue(address, POINTER_TYPE_FLAG);
    }

    public static long getPointer(double v){
        return toBits(v) ^ NAN_MASK ^ TYPE_MASK;
    }

    public static int toIntBits(double v){
        return (int) v;
    }

    public static double fromIntBits(int v){
        return v;
    }

    public static boolean isNullPointer(double v){
        return isPointer(v) && getPointer(v) == NULL_POINTER;
    }

}

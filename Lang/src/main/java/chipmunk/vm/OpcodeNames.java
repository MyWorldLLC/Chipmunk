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

package chipmunk.vm;

public class OpcodeNames {

    // Primitive Operations
    public static final String ADD = "plus";
    public static final String SUB = "minus";
    public static final String MUL = "mul";
    public static final String DIV = "div";
    public static final String FDIV = "fdiv";
    public static final String MOD = "%";
    public static final String POW = "pow";
    public static final String INC = "inc";
    public static final String DEC = "dec";
    public static final String POS = "pos";
    public static final String NEG = "neg";
    public static final String BXOR = "binaryXor";
    public static final String BAND = "binaryAnd";
    public static final String BOR = "binaryOr";
    public static final String BNEG = "binaryNeg";
    public static final String LSHIFT = "lShift";
    public static final String RSHIFT = "rShift";
    public static final String URSHIFT = "unsignedRShift";

    // Flow operations
    public static final String CALL = "call";

    // Comparison/Boolean operations
    public static final String EQ = "equals";
    public static final String GT = "compare";
    public static final String LT = "compare";
    public static final String GE = "compare";
    public static final String LE = "compare";

    // Object operations
    public static final String INSTANCEOF = "instanceOf";
    public static final String GETAT = "getAt";
    public static final String SETAT = "setAt";
    public static final String TRUTH = "truth";
    public static final String AS = "as";
    public static final String ITER = "iterator";
    public static final String RANGE = "range";

}

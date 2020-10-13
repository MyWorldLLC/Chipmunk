/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk;

public class NativeTypeLib implements ChipmunkLibrary {

    public static Integer plus(Integer a, Integer b){
        return Integer.sum(a, b);
    }

    public static Integer minus(Integer a, Integer b){
        return Integer.sum(a, -b);
    }

    public static Integer pos(Integer a){
        return Math.abs(a);
    }

    public static Float div(Integer a, Integer b){
        return a / (float)b;
    }

    public static Integer fdiv(Integer a, Integer b){
        return a / b;
    }

    public static Integer mul(Integer a, Integer b){
        return a * b;
    }

    public static Integer pow(Integer a, Integer b){
        return (int) Math.pow(a, b);
    }

    public static Integer mod(Integer a, Integer b){
        return a % b;
    }

    public static Integer compare(Integer a, Integer b){
        return Integer.compare(a, b);
    }

    public static Boolean truth(Integer a){
        return a != 0;
    }

    public static Boolean truth(Boolean a){
        return a;
    }
}

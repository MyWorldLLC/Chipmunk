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

import chipmunk.modules.runtime.CBoolean;
import chipmunk.modules.runtime.CFloat;
import chipmunk.modules.runtime.CInteger;
import chipmunk.modules.runtime.TypeConversionException;
import chipmunk.runtime.ChipmunkClass;
import chipmunk.runtime.ChipmunkObject;
import chipmunk.runtime.FloatRange;
import chipmunk.runtime.IntegerRange;

import java.util.ArrayList;
import java.util.HashMap;

public class NativeTypeLib implements ChipmunkLibrary {

    // ================================ Integer Math ================================
    public static Integer plus(Integer a, Integer b){
        return Integer.sum(a, b);
    }

    public static Integer minus(Integer a, Integer b){
        return Integer.sum(a, -b);
    }

    public static Integer pos(Integer a){
        return Math.abs(a);
    }

    public static IntegerRange range(Integer start, Integer end, Boolean inclusive){
        return new IntegerRange(start, end, 1, inclusive);
    }

    public static IntegerRange range(Integer start, Integer end, Integer step, Boolean inclusive){
        return new IntegerRange(start, end, step, inclusive);
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

    public static Boolean equals(Integer a, Integer b){
        return a.equals(b);
    }

    public static Boolean truth(Integer a){
        return a != 0;
    }

    // ================================ Float Math ================================

    public static FloatRange range(Float start, Float end, Boolean inclusive){
        return new FloatRange(start, end, 1.0f, inclusive);
    }

    public static FloatRange range(Float start, Float end, Float step, Boolean inclusive){
        return new FloatRange(start, end, step, inclusive);
    }

    // ================================ Boolean Operations ================================

    public static Boolean truth(Boolean a){
        return a;
    }

    public static Object as(Boolean value, Class<?> otherType){
        if(otherType == Integer.class){
            return value ? 1 : 0;
        }else if(otherType == Float.class){
            return value ? 1.0f : 0.0f;
        }else if(otherType == Boolean.class){
            return value;
        }else{
            throw new TypeConversionException(String.format("Cannot convert boolean to %s", otherType.getSimpleName()), value, otherType);
        }
    }

    // ================================ Collection Operations ================================

    public static void add(ArrayList<Object> a, Object element){
        a.add(element);
    }

    public static Object getAt(ArrayList<Object> a, Integer element){
        return a.get(element);
    }

    public static Object setAt(ArrayList<Object> a, Integer index, Object element){
        return a.set(index, element);
    }

    public static Object getAt(HashMap<Object, Object> a, Object key){
        return a.get(key);
    }

    public static Object setAt(HashMap<Object, Object> a, Object key, Object value){
        return a.put(key, value);
    }

    // ================================ Object Operations ================================

    public static ChipmunkClass getClass(ChipmunkObject o){
        return o.getChipmunkClass();
    }
}

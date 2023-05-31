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

package chipmunk.runtime;

import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.ChipmunkVM;
import chipmunk.vm.invoke.ChipmunkLibrary;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

import java.util.*;

public class NativeTypeLib implements ChipmunkLibrary {

    // ================================ Integer Math ================================
    public static Object as(Integer a, Class<?> type){
        switch (type.getName()){
            case "java.lang.Integer" -> {
                return a;
            }
            case "java.lang.Float" -> {
                return Float.valueOf(a);
            }
            case "java.lang.Boolean" -> {
                return a != 0;
            }
            case "java.lang.String" -> {
                return a.toString();
            }
            default -> {
                throw new IllegalArgumentException("Cannot cast integer to " + type.getName());
            }
        }
    }

    public static Integer plus(Integer a, Integer b){
        return a + b;
    }

    public static Float plus(Integer a, Float b){
        return Float.sum(a, b);
    }

    public static Integer minus(Integer a, Integer b){
        return a - b;
    }

    public static Float minus(Integer a, Float b){
        return a - b;
    }

    public static Integer pos(Integer a){
        return Math.abs(a);
    }

    public static Integer neg(Integer a){
        return -a;
    }

    public static Integer inc(Integer a){
        return a + 1;
    }

    public static Integer dec(Integer a){
        return a - 1;
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

    public static Float div(Integer a, Float b){
        return a / b;
    }

    public static Integer fdiv(Integer a, Integer b){
        return a / b;
    }

    public static Integer mul(Integer a, Integer b){
        return a * b;
    }

    public static Float mul(Integer a, Float b){
        return a * b;
    }

    public static Integer pow(Integer a, Integer b){
        return (int) Math.pow(a, b);
    }

    public static Float pow(Integer a, Float b) {
        return (float) Math.pow(a, b);
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
    public static Object as(Float a, Class<?> type){
        switch (type.getName()){
            case "java.lang.Integer" -> {
                return a.intValue();
            }
            case "java.lang.Float" -> {
                return a;
            }
            case "java.lang.String" -> {
                return a.toString();
            }
            default -> {
                throw new IllegalArgumentException("Cannot cast float to " + type.getName());
            }
        }
    }
    public static Float plus(Float a, Float b){
        return a + b;
    }

    public static Float plus(Float a, Integer b){
        return a + b;
    }

    public static Float minus(Float a, Float b){
        return a - b;
    }

    public static Float minus(Float a, Integer b){
        return a - b;
    }

    public static Float mul(Float a, Float b){
        return a * b;
    }

    public static Float mul(Float a, Integer b){
        return a * b;
    }

    public static Float div(Float a, Float b){
        return a / b;
    }

    public static Float div(Float a, Integer b){
        return a / (float)b;
    }

    public static Float pos(Float a){
        return Math.abs(a);
    }

    public static Float neg(Float a){
        return -a;
    }

    public static Float inc(Float a){
        return a + 1;
    }

    public static Float dec(Float a){
        return a - 1;
    }

    public static Float pow(Float a, Integer b){
        return (float) Math.pow(a, b);
    }

    public static Float pow(Float a, Float b){
        return (float) Math.pow(a, b);
    }

    public static Integer compare(Float a, Float b){
        return Float.compare(a, b);
    }

    public static Integer compare(Float a, Integer b){
        return Float.compare(a, b);
    }

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
        if(otherType.equals(Integer.class)){
            return value ? 1 : 0;
        }else if(otherType.equals(Float.class)){
            return value ? 1.0f : 0.0f;
        }else if(otherType.equals(Boolean.class)){
            return value;
        }else{
            throw new IllegalArgumentException(String.format("Cannot convert boolean to %s", otherType.getSimpleName()));
        }
    }

    // ================================ String Operations ================================
    public static String plus(String a, Object b){
        return a + b;
    }

    public static String format(String a, ArrayList<Object> args){
        return a.formatted(args.toArray());
    }

    public static String mod(String a, ArrayList<Object> args){
        return format(a, args);
    }

    // ================================ Map Operations ================================
    public static Object getAt(HashMap<Object, Object> a, Object key){
        return a.get(key);
    }

    public static Object setAt(HashMap<Object, Object> a, Object key, Object value){
        return a.put(key, value);
    }

    public static Object remove(HashMap<Object, Object> a, Object key){
        return a.remove(key);
    }

    public static Boolean remove(HashMap<Object, Object> a, Object key, Object value){
        return a.remove(key, value);
    }

    public static Iterator<Object> iterator(HashMap<Object, Object> a){
        return new Iterator<>() {

            private Iterator<Object> it = a.keySet().iterator();

            @AllowChipmunkLinkage
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @AllowChipmunkLinkage
            @Override
            public Object next() {
                return it.next();
            }
        };
    }

    public static String toString(HashMap<Object, Object> a){
        return a.toString();
    }

    // ================================ List Operations ================================

    public static void add(ArrayList<Object> a, Object element){
        a.add(element);
    }

    public static void add(ArrayList<Object> a, Integer i, Object element){
        if (i < 0) {
            a.add(a.size() + i, element);
        } else {
            a.add(i, element);
        }
    }

    protected static int listIndex(List<Object> a, int i){
        return i < 0 ? a.size() + i : i;
    }

    public static Object getAt(ArrayList<Object> a, Integer i){
        return a.get(listIndex(a, i));
    }

    public static ArrayList<Object> getAt(ArrayList<Object> a, IntegerRange r) {
        int beginIndex = listIndex(a, r.getStart());
        int endIndex = r.isInclusive() ? listIndex(a, r.getEnd()) + 1 : listIndex(a, r.getEnd());
        ArrayList<Object> newList = new ArrayList<>(endIndex - beginIndex);
        for(int i = beginIndex; i < endIndex; i++){
            newList.add(a.get(i));
        }
        return newList;
    }

    public static Object setAt(ArrayList<Object> a, Integer i, Object element){
        return a.set(listIndex(a, i), element);
    }

    public static Object remove(ArrayList<Object> a, Integer i){
        return i < 0 ? a.remove(a.size() + i) : a.remove(i);
    }

    public static Iterator<Object> iterator(ArrayList<Object> a){
        return new Iterator<>() {

            private Iterator<Object> it = a.iterator();

            @AllowChipmunkLinkage
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @AllowChipmunkLinkage
            @Override
            public Object next() {
                return it.next();
            }
        };
    }

    public static <T extends Comparable> ArrayList<T> sort(ArrayList<T> a){
        Collections.sort(a, Comparator.naturalOrder());
        return a;
    }

    public static ArrayList<Object> sort(ArrayList<Object> a, Object comparator){
        ChipmunkScript script = ChipmunkScript.getCurrentScript();
        ChipmunkVM vm = script.getVM();
        Collections.sort(a, (b, c) ->
            (Integer) vm.invoke(script, comparator, "compare", new Object[]{b, c})
        );
        return a;
    }

    public static String toString(ArrayList<Object> a){
        return a.toString();
    }

    // ================================ Object Operations ================================

    public static ChipmunkClass getClass(ChipmunkObject o){
        return o.getChipmunkClass();
    }

    public static Boolean equals(Object a, Object b){
        return a == null ? b == null : a.equals(b);
    }

}

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

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class CRuntime {

    public static int ipow(int a, int b){
        return (int) Math.pow(a, b);
    }

    public static long lpow(long a, long b){
        return (long) Math.pow(a, b);
    }

    public static float lpow(float a, float b){
        return (float) Math.pow(a, b);
    }

    public static double dpow(double a, double b){
        return Math.pow(a, b);
    }

    public static float idiv(int a, int b){
        return (float) a / (float) b;
    }

    public static double ldiv(long a, long b){
        return (double) a / (double) b;
    }

    public static int ifdiv(int a, int b){
        return Math.floorDiv(a, b);
    }

    public static long lfdiv(long a, long b){
        return Math.floorDiv(a, b);
    }

    public static float ffdiv(float a, float b){
        return (float) Math.floor(a / b);
    }

    public static double dfdiv(double a, double b){
        return Math.floor(a / b);
    }

    public static String format(String format, List<Object> args){
        return String.format(format, args);
    }

    public static int iinc(int a){
        return a + 1;
    }

    public static long linc(long a){
        return a + 1;
    }

    public static float finc(float a){
        return a + 1;
    }

    public static double dinc(double a){
        return a + 1;
    }

    public static int idec(int a){
        return a - 1;
    }

    public static long ldec(long a){
        return a - 1;
    }

    public static float fdec(float a){
        return a - 1;
    }

    public static double ddec(double a){
        return a - 1;
    }

    public static int ibneg(int a){
        return ~a;
    }

    public static long lbneg(long a){
        return ~a;
    }

    public static int ipos(int a){
        return Math.abs(a);
    }

    public static long lpos(long a){
        return Math.abs(a);
    }

    public static float fpos(float a){
        return Math.abs(a);
    }

    public static double dpos(double a){
        return Math.abs(a);
    }

    public static boolean and(boolean a, boolean b){
        return a && b;
    }

    public static boolean or(boolean a, boolean b){
        return a || b;
    }

    public static boolean not(boolean a){
        return !a;
    }

    public static boolean ilt(int a, int b){
        return a < b;
    }

    public static boolean llt(long a, long b){
        return a < b;
    }

    public static boolean flt(float a, float b){
        return a < b;
    }

    public static boolean dlt(double a, double b){
        return a < b;
    }

    public static boolean slt(String a, String b){
        return Objects.compare(a, b, String::compareTo) < 0;
    }

    public static boolean ile(int a, int b){
        return a <= b;
    }

    public static boolean lle(long a, long b){
        return a <= b;
    }

    public static boolean fle(float a, float b){
        return a <= b;
    }

    public static boolean dle(double a, double b){
        return a <= b;
    }

    public static boolean sle(String a, String b){
        return Objects.compare(a, b, String::compareTo) <= 0;
    }

    public static boolean igt(int a, int b){
        return a > b;
    }

    public static boolean lgt(long a, long b){
        return a > b;
    }

    public static boolean fgt(float a, float b){
        return a > b;
    }

    public static boolean dgt(double a, double b){
        return a > b;
    }

    public static boolean sgt(String a, String b){
        return Objects.compare(a, b, String::compareTo) > 0;
    }

    public static boolean ige(int a, int b){
        return a >= b;
    }

    public static boolean lge(long a, long b){
        return a >= b;
    }

    public static boolean fge(float a, float b){
        return a >= b;
    }

    public static boolean dge(double a, double b){
        return a >= b;
    }

    public static boolean sge(String a, String b){
        return Objects.compare(a, b, String::compareTo) >= 0;
    }

    public static boolean beq(boolean a, boolean b){
        return a == b;
    }

    public static boolean ieq(int a, int b){
        return a == b;
    }

    public static boolean leq(long a, long b){
        return a == b;
    }

    public static boolean feq(float a, float b){
        return a == b;
    }

    public static boolean deq(double a, double b){
        return a == b;
    }

    public static boolean objEq(Object a, Object b){
        return Objects.equals(a, b);
    }

    public static int b2i(boolean a){
        return a ? 1 : 0;
    }

    public static boolean i2b(int a){
        return a != 0;
    }

    public static boolean l2b(long a){
        return a != 0;
    }

    public static boolean f2b(float a){
        return a != 0;
    }

    public static boolean d2b(double a){
        return a != 0;
    }

    public static String b2s(boolean a){
        return Boolean.toString(a);
    }

    public static String i2s(int a){
        return Integer.toString(a);
    }

    public static String l2s(long a){
        return Long.toString(a);
    }

    public static String f2s(float a){
        return Float.toString(a);
    }

    public static String d2s(double a){
        return Double.toString(a);
    }

    public static Object b2a(boolean a){
        return a;
    }

    public static Object b2a(byte a){
        return a;
    }

    public static Object s2a(short a){
        return a;
    }

    public static Object i2a(int a){
        return a;
    }

    public static Object l2a(long a){
        return a;
    }

    public static Object f2a(float a){
        return a;
    }

    public static Object d2a(double a){
        return a;
    }

    public static Fiber.Frame frame(String method, int suspensionPoint, int stackSize, int localsSize) {
        return new Fiber.Frame(method, suspensionPoint, stackSize, localsSize);
    }

}

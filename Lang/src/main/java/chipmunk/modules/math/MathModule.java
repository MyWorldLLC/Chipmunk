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

package chipmunk.modules.math;

import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

@AllowChipmunkLinkage
public class MathModule implements ChipmunkModule {

    public static final String MATH_MODULE_NAME = "chipmunk.math";

    public static final double PI = java.lang.Math.PI;
    public static final double E = java.lang.Math.E;

    public Float abs(Float value){
        return java.lang.Math.abs(value);
    }

    public Integer abs(Integer value){
        return java.lang.Math.abs(value);
    }

    public Float acos(Float value){
        return (float) java.lang.Math.acos(value);
    }

    public Integer addExact(Integer a, Integer b){
        return java.lang.Math.addExact(a, b);
    }

    public Float asin(Float value){
        return (float) java.lang.Math.asin(value);
    }

    public Float atan(Float value){
        return (float) java.lang.Math.atan(value);
    }

    public Float atan2(Float a, Float b){
        return (float) java.lang.Math.atan2(a, b);
    }

    public Float cbrt(Float value){
        return (float) java.lang.Math.cbrt(value);
    }

    public Float ceil(Float value){
        return (float) java.lang.Math.ceil(value);
    }

    public Float cos(Float value){
        return (float) java.lang.Math.cos(value);
    }

    public Float cosh(Float value){
        return (float) java.lang.Math.cosh(value);
    }

    public Float exp(Float value){
        return (float) java.lang.Math.exp(value);
    }

    public Float floor(Float value){
        return (float) java.lang.Math.floor(value);
    }

    public Float log(Float value){
        return (float) java.lang.Math.log(value);
    }

    public Float log10(Float value){
        return (float) java.lang.Math.log10(value);
    }

    public Float max(Float a, Float b){
        return java.lang.Math.max(a, b);
    }

    public Integer max(Integer a, Integer b){
        return java.lang.Math.max(a, b);
    }

    public Float min(Float a, Float b){
        return java.lang.Math.min(a, b);
    }

    public Integer min(Integer a, Integer b){
        return java.lang.Math.min(a, b);
    }

    public Float pow(Float base, Float exp){
        return (float) java.lang.Math.pow(base, exp);
    }

    public Float random(){
        return (float) java.lang.Math.random();
    }

    public Integer round(Float value){
        return java.lang.Math.round(value);
    }

    public Float signum(Float value){
        return java.lang.Math.signum(value);
    }

    public Float sin(Float value){
        return (float) java.lang.Math.sin(value);
    }

    public Float sinh(Float value){
        return (float) java.lang.Math.sinh(value);
    }

    public Float sqrt(Float value){
        return (float) java.lang.Math.sqrt(value);
    }

    public Float tan(Float value){
        return (float) java.lang.Math.tan(value);
    }

    public Float tanh(Float value){
        return (float) java.lang.Math.tanh(value);
    }

    public Float degrees(Float rad){
        return (float) java.lang.Math.toDegrees(rad);
    }

    public Float radians(Float deg){
        return (float) java.lang.Math.toRadians(deg);
    }

    @Override
    public String getName(){
        return MATH_MODULE_NAME;
    }

}

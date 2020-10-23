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

import chipmunk.ChipmunkVM;
import chipmunk.modules.runtime.CFloat;
import chipmunk.modules.runtime.CInteger;

public class CMath {

    public CFloat abs(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat(Math.abs(value.floatValue()));
    }

    public CInteger abs(ChipmunkVM vm, CInteger value){
        //vm.traceInteger();
        return new CInteger(Math.abs(value.intValue()));
    }

    public CFloat acos(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat((float)Math.acos(value.floatValue()));
    }

    public CInteger addExact(ChipmunkVM vm, CInteger l, CInteger r){
        //vm.traceInteger();
        return new CInteger(Math.addExact(l.getValue(), r.getValue()));
    }

    public CFloat asin(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat((float)Math.asin(value.floatValue()));
    }

    public CFloat atan(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat((float)Math.atan(value.floatValue()));
    }

    public CFloat atan2(ChipmunkVM vm, CFloat l, CFloat r){
        //vm.traceFloat();
        return new CFloat((float)Math.atan2(l.floatValue(), r.floatValue()));
    }

    public CFloat cbrt(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat((float)Math.cbrt(value.floatValue()));
    }

    public CFloat ceil(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat((float)Math.ceil(value.floatValue()));
    }

    public CFloat cos(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat((float)Math.cos(value.floatValue()));
    }

    public CFloat cosh(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat((float)Math.cosh(value.floatValue()));
    }

    public CFloat exp(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat((float)Math.exp(value.floatValue()));
    }

    public CFloat floor(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat((float)Math.floor(value.floatValue()));
    }

    public CFloat log(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat((float)Math.log(value.floatValue()));
    }

    public CFloat log10(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat((float)Math.log10(value.floatValue()));
    }

    public CFloat max(ChipmunkVM vm, CFloat l, CFloat r){
        //vm.traceFloat();
        return new CFloat((float)Math.max(l.floatValue(), r.floatValue()));
    }

    public CInteger max(ChipmunkVM vm, CInteger l, CInteger r){
        //vm.traceInteger();
        return new CInteger(Math.max(l.intValue(), r.intValue()));
    }

    public CFloat min(ChipmunkVM vm, CFloat l, CFloat r){
        //vm.traceFloat();
        return new CFloat(Math.min(l.floatValue(), r.floatValue()));
    }

    public CInteger min(ChipmunkVM vm, CInteger l, CInteger r){
        //vm.traceInteger();
        return new CInteger(Math.min(l.intValue(), r.intValue()));
    }

    public CFloat pow(ChipmunkVM vm, CFloat base, CFloat exp){
        //vm.traceFloat();
        return new CFloat((float)Math.pow(base.floatValue(), exp.floatValue()));
    }

    public CFloat random(ChipmunkVM vm){
        //vm.traceFloat();
        return new CFloat((float)Math.random());
    }

    public CInteger round(ChipmunkVM vm, CFloat value){
        //vm.traceInteger();
        return new CInteger(Math.round(value.floatValue()));
    }

    public CFloat signum(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat(Math.signum(value.floatValue()));
    }

    public CFloat sin(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat((float)Math.sin(value.floatValue()));
    }

    public CFloat sinh(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat((float)Math.sinh(value.floatValue()));
    }

    public CFloat sqrt(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat((float)Math.sqrt(value.floatValue()));
    }

    public CFloat tan(ChipmunkVM vm, CFloat value){
        //vm.traceFloat();
        return new CFloat((float)Math.tan(value.floatValue()));
    }

    public CFloat tanh(ChipmunkVM vm, CFloat value){
       // vm.traceFloat();
        return new CFloat((float)Math.tanh(value.floatValue()));
    }

    public CFloat degrees(ChipmunkVM vm, CFloat rad){
        //vm.traceFloat();
        return new CFloat((float)Math.toDegrees(rad.floatValue()));
    }

    public CFloat radians(ChipmunkVM vm, CFloat deg){
        //vm.traceFloat();
        return new CFloat((float)Math.toRadians(deg.floatValue()));
    }

}

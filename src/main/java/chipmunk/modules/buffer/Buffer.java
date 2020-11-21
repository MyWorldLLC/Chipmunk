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

package chipmunk.modules.buffer;

import chipmunk.vm.ChipmunkVM;
import chipmunk.modules.runtime.CBoolean;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CInteger;
import chipmunk.modules.runtime.CNull;

import java.util.Arrays;

public class Buffer {

    protected final BufferCClass cClass;
    private byte[] data;

    public Buffer(BufferCClass cls, int initialSize){
        cClass = cls;
        data = new byte[initialSize];
    }

    public CInteger getAt(ChipmunkVM vm, CInteger index){
        return new CInteger(data[index.intValue()]);
    }

    public CInteger setAt(ChipmunkVM vm, CInteger index, CInteger value){
        data[index.intValue()] = (byte) value.intValue();
        return value;
    }

    public CInteger size(ChipmunkVM vm){
        return new CInteger(data.length);
    }

    public CNull resize(ChipmunkVM vm, CInteger newSize){

        final int size = newSize.intValue();
        //vm.traceMem(size - data.length); // This will correctly trace size reductions too

        data = Arrays.copyOf(data, size);

        return CNull.instance();
    }

    public CClass getClass(ChipmunkVM vm){
        return cClass;
    }

    public byte[] getData(){
        return data;
    }

    public void setData(byte[] data){
        this.data = data;
    }

    public CBoolean equals(ChipmunkVM vm, Object other){
        if(other instanceof Buffer){
            return new CBoolean(Arrays.equals(data, ((Buffer) other).data));
        }
        return new CBoolean(false);
    }
}

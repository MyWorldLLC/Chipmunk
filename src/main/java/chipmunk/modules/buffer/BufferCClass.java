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

import chipmunk.ChipmunkVM;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CInteger;
import chipmunk.modules.runtime.CModule;

public class BufferCClass extends CClass {

    public BufferCClass(CModule module) {
        super("Buffer", module);
    }

    public Buffer call(ChipmunkVM vm, Object[] params){
        if(params.length != 0 || params.length != 1){
            throw new IllegalArgumentException(String.format("Buffer can only be instantiated with 0 or 1 parameters, not %d", params.length));
        }

        int size = 0;
        if(params.length == 1) {
            size = ((CInteger) params[0]).intValue();
        }

        //vm.traceReference();
        //vm.traceMem(size);
        return new Buffer(this, size);
    }

    public Buffer instantiate(){
        return new Buffer(this, 0);
    }

    public Buffer create(int initialSize){
        return new Buffer(this, initialSize);
    }
}

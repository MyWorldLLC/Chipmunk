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

import java.util.Arrays;

public class Buffer {

    private byte[] data;

    public Buffer(int initialSize){
        // TODO - VM memory usage check
        data = new byte[initialSize];
    }

    public Integer getAt(Integer index){
        return (int) data[index];
    }

    public Integer setAt(Integer index, Integer value){
        data[index] = (byte) value.intValue();
        return value;
    }

    public Integer size(){
        return data.length;
    }

    public void resize(Integer newSize){
        // TODO - VM memory usage check
        data = Arrays.copyOf(data, newSize);
    }

    public byte[] getData(){
        return data;
    }

    public void setData(byte[] data){
        this.data = data;
    }

    public boolean equals(Object other){
        if(other instanceof Buffer){
            return Arrays.equals(data, ((Buffer) other).data);
        }
        return false;
    }
}

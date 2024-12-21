/*
 * Copyright (C) 2024 MyWorld, LLC
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

package chipmunk.runtime.hvm;

import myworld.hummingbird.HummingbirdVM;

public class Memory {

    public static long read(long pointer, CType type, HummingbirdVM vm){
        return switch (type){
            case INT, FLOAT -> vm.memory.getInt((int)pointer);
            case LONG, DOUBLE -> vm.memory.getLong((int)pointer);
        };
    }

    public static void write(long pointer, long value, CType type, HummingbirdVM vm){
        switch (type){
            case INT, FLOAT -> vm.memory.putInt((int) pointer, (int) value);
            case LONG, DOUBLE -> vm.memory.putLong((int) pointer, value);
        }
    }

    public static long readField(long pointer, int field, CClass type, HummingbirdVM vm){
        var f = type.fields[field];
        return read(f.calculateAddress(pointer), f.pType(), vm);
    }

    public static void writeField(long pointer, long value, int field, CClass type, HummingbirdVM vm){
        var f = type.fields[field];
        var addr = f.calculateAddress(pointer);
        write(f.calculateAddress(addr), value, f.pType(), vm);
    }

    // TODO - support shared fields in memory model

}

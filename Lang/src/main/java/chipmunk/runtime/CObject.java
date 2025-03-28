/*
 * Copyright (C) 2025 MyWorld, LLC
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

public class CObject {

    public CClass cls;
    public Object[] fields;

    // TODO - move to nodes for consistency with call/callAt

    public Object getField(String name){
        var idx = cls.getFieldIndex(name);
        if(idx == -1){
            // TODO - trait lookup & resolution
            // TODO - throw exception
        }
        return fields[idx];
    }

    public Object getField(int index){
        if(index < 0 || index > fields.length){
            // TODO - throw exception
        }
        return fields[index];
    }

}

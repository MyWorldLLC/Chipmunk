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

package chipmunk.binary;

import java.util.ArrayList;
import java.util.List;

public class BinaryNamespace {

    public static class Entry {
        protected final String name;
        protected final byte flags;
        protected final Object value;

        public Entry(String name, byte flags, Object value){
            this.name = name;
            this.flags = flags;
            this.value = value;
        }

        public String getName(){
            return name;
        }

        public byte getFlags(){
            return flags;
        }

        public Object getValue(){
            return value;
        }
    }

    protected List<Entry> entries;

    public BinaryNamespace(){
        this(0);
    }

    public BinaryNamespace(int size){
        entries = new ArrayList<>(size);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries){
        this.entries = entries;
    }
}

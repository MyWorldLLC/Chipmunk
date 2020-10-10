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
import java.util.Iterator;
import java.util.List;

public class BinaryNamespace implements Iterable<BinaryNamespace.Entry> {

    public static class Entry {
        protected final String name;
        protected final byte flags;
        protected final FieldType type;
        protected final Object classOrMethod;

        public Entry(String name, byte flags){
            this(name, flags, FieldType.DYNAMIC_VAR);
        }

        public Entry(String name, byte flags, FieldType type){
            this.name = name;
            this.flags = flags;
            this.type = type;
            classOrMethod = null;
        }

        public Entry(String name, byte flags, BinaryMethod method){
            this.name = name;
            this.flags = flags;
            this.type = FieldType.METHOD;
            classOrMethod = method;
        }

        public Entry(String name, byte flags, BinaryClass cls){
            this.name = name;
            this.flags = flags;
            this.type = FieldType.CLASS;
            classOrMethod = cls;
        }

        public static Entry makeField(String name, byte flags){
            return new Entry(name, flags, FieldType.DYNAMIC_VAR);
        }

        public static Entry makeClass(String name, byte flags, BinaryClass cls){
            return new Entry(name, flags, cls);
        }

        public static Entry makeMethod(String name, byte flags, BinaryMethod method){
            return new Entry(name, flags, method);
        }

        public String getName(){
            return name;
        }

        public byte getFlags(){
            return flags;
        }

        public FieldType getType(){
            return type;
        }

        public BinaryClass getBinaryClass(){
            return (BinaryClass) classOrMethod;
        }

        public BinaryMethod getBinaryMethod(){
            return (BinaryMethod) classOrMethod;
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

    public void addEntry(BinaryNamespace.Entry entry){
        entries.add(entry);
    }

    public boolean has(String symbol){

        for(Entry e : entries){
            if(e.getName().equals(symbol)){
                return true;
            }
        }

        return false;
    }

    public Object get(String symbol){

        for(Entry e : entries){
            if(e.getName().equals(symbol)){
                FieldType type = e.getType();

                if(type == FieldType.CLASS){
                    return e.getBinaryClass();
                }else if(type == FieldType.METHOD){
                    return e.getBinaryMethod();
                }

            }
        }

        return null;
    }

    @Override
    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

}

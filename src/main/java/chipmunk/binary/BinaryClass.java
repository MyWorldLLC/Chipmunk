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

public class BinaryClass {

    protected String name;
    protected BinaryModule module;
    protected BinaryMethod instanceInitializer;
    protected BinaryMethod sharedInitializer;
    protected BinaryNamespace instanceFields;
    protected BinaryNamespace sharedFields;

    public BinaryClass(){
        instanceFields = new BinaryNamespace();
        sharedFields = new BinaryNamespace();
    }

    public BinaryClass(String name){
        this(name, null);
    }

    public BinaryClass(String name, BinaryModule module){
        this();
        this.name = name;
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BinaryModule getModule() {
        return module;
    }

    public void setModule(BinaryModule module) {
        this.module = module;
    }

    public BinaryMethod getInstanceInitializer() {
        return instanceInitializer;
    }

    public void setInstanceInitializer(BinaryMethod instanceInitializer) {
        this.instanceInitializer = instanceInitializer;
    }

    public BinaryMethod getSharedInitializer() {
        return sharedInitializer;
    }

    public void setSharedInitializer(BinaryMethod sharedInitializer) {
        this.sharedInitializer = sharedInitializer;
    }

    public BinaryNamespace getInstanceFields() {
        return instanceFields;
    }

    public void setInstanceFields(BinaryNamespace instanceFields) {
        this.instanceFields = instanceFields;
    }

    public BinaryNamespace getSharedFields() {
        return sharedFields;
    }

    public void setSharedFields(BinaryNamespace sharedFields) {
        this.sharedFields = sharedFields;
    }
}

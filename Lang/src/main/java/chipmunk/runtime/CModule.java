/*
 * Copyright (C) 2026 MyWorld, LLC
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

import java.util.Arrays;

public class CModule implements ChipmunkModule {

    protected final String name;
    protected final String fileName;
    protected Object[] constantPool;

    protected double[] fields;
    protected CField[] fieldDefs;

    protected CMethod[] methods;
    protected boolean initialized;

    public CModule(String name, String fileName) {
        this.name = name;
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public double[] getFields() {
        return fields;
    }

    public CField[] getFieldDefs() {
        return fieldDefs;
    }

    public Object[] constants(){
        return constantPool;
    }

    public CMethod getMethod(String name){
        return Arrays.stream(methods)
                .filter(m -> m.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void setFields(CField[] fields){
        this.fields = new double[fields.length];
        this.fieldDefs = fields;
    }

    public void setMethods(CMethod[] methods){
        this.methods = methods;
    }

    public void setClasses(CClass[] classes){
    }

    public void setConstantPool(Object[] constantPool){
        this.constantPool = constantPool;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void markInitialized(){
        initialized = true;
    }
}

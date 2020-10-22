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

public class BinaryModule {

    protected String name;
    protected Object[] constantPool;
    protected BinaryImport[] imports;
    protected BinaryNamespace namespace;

    public BinaryModule(){
        namespace = new BinaryNamespace();
    }

    public BinaryModule(String name){
        this();
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public BinaryImport[] getImports() {
        return imports;
    }

    public void setImports(BinaryImport[] imports) {
        this.imports = imports;
    }

    public BinaryNamespace getNamespace(){
        return namespace;
    }

    public void setNamespace(BinaryNamespace namespace){
        this.namespace = namespace;
    }

    public Object[] getConstantPool(){
        return constantPool;
    }

    public void setConstantPool(Object[] constantPool){
        this.constantPool = constantPool;
    }

}

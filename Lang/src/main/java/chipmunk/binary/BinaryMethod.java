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

public class BinaryMethod {

    protected int argCount;
    protected int defaultArgCount;
    protected int localCount;
    protected int upvalueRefCount;
    protected int upvalueLocalCount;

    protected byte[] instructions;
    protected ExceptionBlock[] exceptionTable;
    protected DebugEntry[] debugTable;
    protected String declarationSymbol;

    protected BinaryModule module;

    public BinaryMethod(){
        localCount = 1;
    }

    public int getArgCount(){
        return argCount;
    }

    public void setArgCount(int count){
        argCount = count;
    }

    public int getDefaultArgCount(){
        return defaultArgCount;
    }

    public void setDefaultArgCount(int count){
        defaultArgCount = count;
    }

    public int getLocalCount(){
        return localCount;
    }

    public void setLocalCount(int count){
        localCount = count;
    }

    public void setUpvalueRefCount(int count){
        upvalueRefCount = count;
    }

    public int getUpvalueRefCount(){
        return upvalueRefCount;
    }

    public void setUpvalueLocalCount(int count){
        upvalueLocalCount = count;
    }

    public int getUpvalueLocalCount(){
        return upvalueLocalCount;
    }

    public Object[] getConstantPool(){
        return module.getConstantPool();
    }

    public void setExceptionTable(ExceptionBlock[] table) {
        exceptionTable = table;
    }

    public ExceptionBlock[] getExceptionTable() {
        return exceptionTable;
    }

    public void setDebugTable(DebugEntry[] table) {
        debugTable = table;
    }

    public DebugEntry[] getDebugTable() {
        return debugTable;
    }

    public String getDeclarationSymbol() {
        return declarationSymbol;
    }

    public void setDeclarationSymbol(String symbol) {
        declarationSymbol = symbol;
    }

    public void setCode(byte[] codeSegment) {
        instructions = codeSegment;
    }

    public byte[] getCode(){
        return instructions;
    }

    public BinaryModule getModule(){
        return module;
    }

    public void setModule(BinaryModule module){
        this.module = module;
    }
}

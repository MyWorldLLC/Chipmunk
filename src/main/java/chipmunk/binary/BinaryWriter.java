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

import chipmunk.DebugEntry;
import chipmunk.ExceptionBlock;
import chipmunk.Namespace;
import chipmunk.modules.runtime.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static chipmunk.binary.BinaryConstants.ConstantType.*;

public class BinaryWriter {

    public void writeModule(OutputStream os, CModule module) throws IOException, IllegalConstantTypeException {

        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(BinaryConstants.CHIPMUNK_BINARY_IDENTIFIER);
        dos.writeShort(BinaryConstants.BINARY_VERSION);

        dos.writeUTF(module.getName());
        writeConstants(dos, module.getConstants());
        writeImports(dos, module.getImports());
        writeObject(dos, module.getInitializer());
        writeNamespace(dos, module.getNamespace());
    }

    protected void writeConstants(DataOutputStream os, List<Object> objs) throws IOException, IllegalConstantTypeException {

        os.writeInt(objs.size());
        for(Object o : objs){
            writeObject(os, o);
        }

    }

    protected void writeImports(DataOutputStream os, List<CModule.Import> imports) throws IOException {

        os.writeInt(imports.size());
        for(CModule.Import i : imports){

            os.writeUTF(i.getName());
            os.writeBoolean(i.isImportAll());
            writeStrings(os, i.getSymbols());
            writeStrings(os, i.getAliases());

        }
    }

    protected void writeNamespace(DataOutputStream os, Namespace namespace) throws IOException, IllegalConstantTypeException {

        BinaryNamespace binNamespace = new BinaryNamespace();
        for(String symbol : namespace.names()){
            byte flags = 0;

            if(namespace.finalNames() != null && namespace.finalNames().contains(symbol)){
                flags |= BinaryConstants.FINAL_FLAG;
            }

            if(namespace.traitNames() != null && namespace.traitNames().contains(symbol)){
                flags |= BinaryConstants.TRAIT_FLAG;
            }

            binNamespace.getEntries().add(new BinaryNamespace.Entry(symbol, flags, namespace.get(symbol)));
        }

        os.writeInt(binNamespace.getEntries().size());
        for(BinaryNamespace.Entry e : binNamespace.getEntries()){
            os.writeUTF(e.getName());
            os.writeByte(e.getFlags());
            writeObject(os, e.getValue());
        }

    }

    protected void writeStrings(DataOutputStream os, List<String> strings) throws IOException {

        os.writeInt(strings.size());

        for(String s : strings){
            os.writeUTF(s);
        }

    }

    protected void writeMethod(DataOutputStream os, CMethod method) throws IOException, IllegalConstantTypeException {

        os.writeUTF(method.getDebugSymbol());
        os.writeInt(method.getLocalCount());
        os.writeInt(method.getArgCount());
        os.writeInt(method.getDefaultArgCount());

        byte[] instructions = method.getInstructions();
        os.writeInt(instructions.length);
        os.write(instructions);

        writeExceptionTable(os, method.getCode().getExceptionTable());
        writeDebugTable(os, method.getCode().getDebugTable());
    }

    protected void writeExceptionTable(DataOutputStream os, ExceptionBlock[] table) throws IOException {

        os.writeInt(table.length);

        for(int i = 0; i < table.length; i++){
            ExceptionBlock b = table[i];

            os.writeInt(b.startIndex);
            os.writeInt(b.catchIndex);
            os.writeInt(b.endIndex);
            os.writeInt(b.exceptionLocalIndex);
        }
    }

    protected void writeDebugTable(DataOutputStream os, DebugEntry[] table) throws IOException {

        os.writeInt(table.length);

        for(int i = 0; i < table.length; i++){
            DebugEntry e = table[i];

            os.writeInt(e.beginIndex);
            os.writeInt(e.endIndex);
            os.writeInt(e.lineNumber);
        }
    }

    protected void writeClass(DataOutputStream os, CClass cls) throws IOException, IllegalConstantTypeException {

        os.writeUTF(cls.getName());
        writeMethod(os, cls.getSharedInitializer());
        writeNamespace(os, cls.getAttributes());

        writeMethod(os, cls.getInstanceInitializer());
        writeNamespace(os, cls.getInstanceAttributes());

    }

    protected void writeObject(DataOutputStream os, Object obj) throws IOException, IllegalConstantTypeException {

        if(obj == null || obj instanceof CNull){
            os.writeByte(NULL.ordinal());
        }else if(obj instanceof Byte){
            os.writeByte(BYTE.ordinal());
            os.writeByte((Byte) obj);
        }else if(obj instanceof Boolean){
            os.writeByte(BOOLEAN.ordinal());
            os.writeBoolean((Boolean) obj);
        }else if(obj instanceof CBoolean){
            os.writeByte(BOOLEAN.ordinal());
            os.writeBoolean(((CBoolean) obj).booleanValue());
        }else if(obj instanceof Short){
            os.writeByte(SHORT.ordinal());
            os.writeShort((Short) obj);
        }else if(obj instanceof Integer){
            os.writeByte(INT.ordinal());
            os.writeInt((Integer) obj);
        }else if(obj instanceof CInteger){
            os.writeByte(INT.ordinal());
            os.writeInt(((CInteger) obj).intValue());
        }else if(obj instanceof Long){
            os.writeByte(LONG.ordinal());
            os.writeLong((Long) obj);
        }else if(obj instanceof Float){
            os.writeByte(FLOAT.ordinal());
            os.writeFloat((Float) obj);
        }else if(obj instanceof CFloat){
            os.writeByte(FLOAT.ordinal());
            os.writeFloat(((CFloat) obj).floatValue());
        }else if(obj instanceof Double){
            os.writeByte(DOUBLE.ordinal());
            os.writeDouble((Double) obj);
        }else if(obj instanceof String){
            os.writeByte(STRING.ordinal());
            os.writeUTF((String) obj);
        }else if(obj instanceof CString){
            os.writeByte(STRING.ordinal());
            os.writeUTF(((CString) obj).stringValue());
        }else if(obj instanceof CMethod){
            os.writeByte(METHOD.ordinal());
            writeMethod(os, (CMethod) obj);
        }else if(obj instanceof CClass){
            os.writeByte(CLASS.ordinal());
            writeClass(os, (CClass) obj);
        }else{
            throw new IllegalConstantTypeException(String.format("%s is not a valid Chipmunk constant", obj.getClass().getName()));
        }
    }

}

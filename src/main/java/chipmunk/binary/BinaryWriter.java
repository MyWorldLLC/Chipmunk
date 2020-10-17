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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static chipmunk.binary.ConstantType.*;

public class BinaryWriter {

    public void writeModule(OutputStream os, BinaryModule module) throws IOException, IllegalConstantTypeException {

        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(BinaryConstants.CHIPMUNK_BINARY_IDENTIFIER);
        dos.writeShort(BinaryConstants.BINARY_VERSION);

        dos.writeUTF(module.getName());
        writeConstants(dos, module.getConstantPool());
        writeImports(dos, module.getImports());
        writeMethod(dos, module.getInitializer());
        writeNamespace(dos, module.getNamespace());

        dos.flush();
    }

    protected void writeConstants(DataOutputStream os, Object[] objs) throws IOException, IllegalConstantTypeException {

        os.writeInt(objs.length);
        for(Object o : objs){
            writeObject(os, o);
        }

    }

    protected void writeImports(DataOutputStream os, BinaryImport[] imports) throws IOException {

        os.writeInt(imports.length);
        for(BinaryImport i : imports){

            os.writeUTF(i.getName());
            os.writeBoolean(i.isImportAll());
            writeStrings(os, i.getSymbols());
            writeStrings(os, i.getAliases());

        }
    }

    protected void writeNamespace(DataOutputStream os, BinaryNamespace namespace) throws IOException, IllegalConstantTypeException {

        os.writeInt(namespace.getEntries().size());
        for(BinaryNamespace.Entry e : namespace.getEntries()){
            os.writeUTF(e.getName());
            os.writeByte(e.getFlags());
            os.writeByte((byte)e.getType().ordinal());
            if(e.getType() == FieldType.METHOD){
                writeMethod(os, e.getBinaryMethod());
            }else if(e.getType() == FieldType.CLASS){
                writeClass(os, e.getBinaryClass());
            }
        }

    }

    protected void writeStrings(DataOutputStream os, String[] strings) throws IOException {

        if(strings == null){
            os.writeInt(0);
            return;
        }

        os.writeInt(strings.length);

        for(String s : strings){
            os.writeUTF(s);
        }

    }

    protected void writeMethod(DataOutputStream os, BinaryMethod method) throws IOException, IllegalConstantTypeException {

        os.writeUTF(method.getDeclarationSymbol());
        os.writeInt(method.getLocalCount());
        os.writeInt(method.getArgCount());
        os.writeInt(method.getDefaultArgCount());

        byte[] instructions = method.getCode();
        os.writeInt(instructions.length);
        os.write(instructions);

        writeExceptionTable(os, method.getExceptionTable());
        writeDebugTable(os, method.getDebugTable());
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

    protected void writeClass(DataOutputStream os, BinaryClass cls) throws IOException, IllegalConstantTypeException {

        os.writeUTF(cls.getName());
        writeMethod(os, cls.getSharedInitializer());
        writeNamespace(os, cls.getSharedFields());

        writeMethod(os, cls.getInstanceInitializer());
        writeNamespace(os, cls.getInstanceFields());

    }

    protected void writeObject(DataOutputStream os, Object obj) throws IOException, IllegalConstantTypeException {

        if(obj == null){
            os.writeByte(NULL.ordinal());
        }else if(obj instanceof Byte){
            os.writeByte(BYTE.ordinal());
            os.writeByte((Byte) obj);
        }else if(obj instanceof Boolean){
            os.writeByte(BOOLEAN.ordinal());
            os.writeBoolean((Boolean) obj);
        }else if(obj instanceof Short){
            os.writeByte(SHORT.ordinal());
            os.writeShort((Short) obj);
        }else if(obj instanceof Integer){
            os.writeByte(INT.ordinal());
            os.writeInt((Integer) obj);
        }else if(obj instanceof Long){
            os.writeByte(LONG.ordinal());
            os.writeLong((Long) obj);
        }else if(obj instanceof Float){
            os.writeByte(FLOAT.ordinal());
            os.writeFloat((Float) obj);
        }else if(obj instanceof Double){
            os.writeByte(DOUBLE.ordinal());
            os.writeDouble((Double) obj);
        }else if(obj instanceof String){
            os.writeByte(STRING.ordinal());
            os.writeUTF((String) obj);
        }else if(obj instanceof BinaryMethod){
            os.writeByte(METHOD.ordinal());
            writeMethod(os, (BinaryMethod) obj);
        }else if(obj instanceof BinaryClass){
            os.writeByte(CLASS.ordinal());
            writeClass(os, (BinaryClass) obj);
        }else{
            throw new IllegalConstantTypeException(String.format("%s is not a valid Chipmunk constant", obj.getClass().getName()));
        }
    }

}

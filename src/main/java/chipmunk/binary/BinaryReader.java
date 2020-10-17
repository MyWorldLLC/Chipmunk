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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BinaryReader {

    protected int maxBufferSize;

    public BinaryReader(){
        maxBufferSize = Integer.MAX_VALUE;
    }

    public BinaryReader(int maxBufferSize){
        this.maxBufferSize = maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize){
        this.maxBufferSize = maxBufferSize;
    }

    public int getMaxBufferSize(){
        return maxBufferSize;
    }

    public BinaryModule readModule(InputStream is) throws IOException, BinaryFormatException {

        try {

            DataInputStream dis = new DataInputStream(is);

            final String binaryIdentifier = dis.readUTF();
            if(!BinaryConstants.CHIPMUNK_BINARY_IDENTIFIER.equals(binaryIdentifier)){
                throw new BinaryFormatException("This is not a Chipmunk binary");
            }

            final short version = dis.readShort();
            if(BinaryConstants.BINARY_VERSION < version){
                throw new BinaryFormatException(
                        String.format(
                                "This parser only supports Chipmunk version %d, but this binary is version %d",
                                BinaryConstants.BINARY_VERSION,
                                version));
            }

            // Read name, constants, imports, & namespace
            String name = dis.readUTF();

            BinaryModule module = new BinaryModule(name);
            module.setConstantPool(readConstants(dis, module));

            BinaryImport[] imports = readImports(dis);
            module.setImports(imports);

            module.setInitializer(readMethod(dis, module));

            module.setNamespace(readNameSpace(dis, module));

            return module;
        }catch(EOFException e){
            throw new BinaryFormatException(e);
        }

    }

    protected Object[] readConstants(DataInputStream is, BinaryModule module) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        Object[] constants = new Object[count];
        for(int i = 0; i < count; i++){
            constants[i] = readObject(is, module);
        }

        return constants;
    }

    protected String[] readStrings(DataInputStream is) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        String[] strings = new String[count];

        for(int i = 0; i < count; i++){
            strings[i] = is.readUTF();
        }

        return strings;
    }

    protected BinaryImport[] readImports(DataInputStream is) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        BinaryImport[] imports = new BinaryImport[count];

        for(int i = 0; i < count; i++){
            final String name = is.readUTF();
            final boolean importAll = is.readBoolean();
            final String[] symbols = readStrings(is);
            final String[] aliases = readStrings(is);

            BinaryImport im = new BinaryImport(name, importAll);
            im.setSymbols(symbols);
            im.setAliases(aliases);
            imports[i] = im;
        }

        return imports;
    }

    protected BinaryNamespace readNameSpace(DataInputStream is, BinaryModule module) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        BinaryNamespace namespace = new BinaryNamespace(count);

        for(int i = 0; i < count; i++){
            final String name = is.readUTF();
            final byte flags = is.readByte();
            final byte typeOrdinal = is.readByte();

            FieldType type = FieldType.values()[typeOrdinal];
            if(type == FieldType.METHOD){
                namespace.getEntries().add(new BinaryNamespace.Entry(name, flags, readMethod(is, module)));
            }else if(type == FieldType.CLASS){
                namespace.getEntries().add(new BinaryNamespace.Entry(name, flags, readClass(is, module)));
            }else{
                namespace.getEntries().add(new BinaryNamespace.Entry(name, flags, type));
            }
        }

        return namespace;
    }

    protected Object readObject(DataInputStream is, BinaryModule module) throws IOException, BinaryFormatException {
        final int typeOrdinal = is.readByte();
        try {
            ConstantType type = ConstantType.values()[typeOrdinal];

            return switch (type) {
                case NULL -> null;
                case BYTE -> is.readByte();
                case BOOLEAN -> is.readBoolean();
                case SHORT -> is.readShort();
                case INT -> is.readInt();
                case LONG -> is.readLong();
                case FLOAT -> is.readFloat();
                case DOUBLE -> is.readDouble();
                case STRING -> is.readUTF();
                case METHOD -> readMethod(is, module);
                case CLASS -> readClass(is, module);
            };
        }catch(ArrayIndexOutOfBoundsException e){
            throw new BinaryFormatException(String.format("%d is not a valid constant type", typeOrdinal), e);
        }
    }

    protected BinaryMethod readMethod(DataInputStream is, BinaryModule module) throws IOException, BinaryFormatException {

        BinaryMethod method = new BinaryMethod();

        method.setDeclarationSymbol(is.readUTF());
        method.setLocalCount(is.readInt());
        method.setArgCount(is.readInt());
        method.setDefaultArgCount(is.readInt());

        final int codeSize = is.readInt();
        checkBufferSize(codeSize);

        method.setCode(is.readNBytes(codeSize));
        method.setExceptionTable(readExceptionTable(is));
        method.setDebugTable(readDebugTable(is));

        method.setModule(module);

        return method;
    }

    protected DebugEntry[] readDebugTable(DataInputStream is) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        DebugEntry[] debugTable = new DebugEntry[count];
        for(int i = 0; i < count; i++){

            DebugEntry entry = new DebugEntry();
            entry.beginIndex = is.readInt();
            entry.endIndex = is.readInt();
            entry.lineNumber = is.readInt();

            debugTable[i] = entry;
        }

        return debugTable;
    }

    protected ExceptionBlock[] readExceptionTable(DataInputStream is) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        ExceptionBlock[] exceptionTable = new ExceptionBlock[count];
        for(int i = 0; i < count; i++){

            ExceptionBlock block = new ExceptionBlock();
            block.startIndex = is.readInt();
            block.catchIndex = is.readInt();
            block.endIndex = is.readInt();
            block.exceptionLocalIndex = is.readInt();

            exceptionTable[i] = block;
        }

        return exceptionTable;
    }

    protected BinaryClass readClass(DataInputStream is, BinaryModule module) throws IOException, BinaryFormatException {

        final String name = is.readUTF();
        BinaryClass cls = new BinaryClass(name, module);

        cls.setSharedInitializer(readMethod(is, module));
        cls.setSharedFields(readNameSpace(is, module));

        cls.setInstanceInitializer(readMethod(is, module));
        cls.setInstanceFields(readNameSpace(is, module));

        return cls;
    }

    protected void checkBufferSize(int size) throws BinaryFormatException {
        if(size > maxBufferSize){
            throw new BinaryFormatException(
                    String.format("Max buffer size is %d but binary contains buffer of size %d",
                            maxBufferSize,
                            size));
        }
    }
}

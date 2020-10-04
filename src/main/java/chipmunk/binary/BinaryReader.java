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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BinaryReader {

    protected final int maxBufferSize;

    public BinaryReader(){
        maxBufferSize = Integer.MAX_VALUE;
    }

    public BinaryReader(int maxBufferSize){
        this.maxBufferSize = maxBufferSize;
    }

    public CModule readModule(InputStream is) throws IOException, BinaryFormatException {

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

            CModule module = new CModule(name);
            module.getConstants().addAll(readConstants(dis, module));

            List<CModule.Import> imports = readImports(dis);
            module.getImports().addAll(imports);

            Namespace moduleNamespace = module.getNamespace();
            BinaryNamespace namespace = readNameSpace(dis, module);

            for(BinaryNamespace.Entry entry : namespace.getEntries()){
                moduleNamespace.set(entry.getName(), entry.getValue());

                if(entry.getValue() instanceof CMethod){
                    ((CMethod) entry.getValue()).bind(module);
                }

                final int flags = entry.getFlags();
                if((flags & BinaryConstants.FINAL_FLAG) != 0){
                    moduleNamespace.markFinal(entry.getName());
                }
            }

            return module;
        }catch(EOFException e){
            throw new BinaryFormatException(e);
        }

    }

    protected List<Object> readConstants(DataInputStream is, CModule module) throws IOException, BinaryFormatException {
        List<Object> constants = new ArrayList<>();

        final int count = is.readInt();
        checkBufferSize(count);
        for(int i = 0; i < count; i++){
            constants.add(readObject(is, module));
        }

        return constants;
    }

    protected List<String> readStrings(DataInputStream is) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        List<String> strings = new ArrayList<>(count);

        for(int i = 0; i < count; i++){
            strings.add(is.readUTF());
        }

        return strings;
    }

    protected List<CModule.Import> readImports(DataInputStream is) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        List<CModule.Import> imports = new ArrayList<>(count);

        for(int i = 0; i < count; i++){
            final String name = is.readUTF();
            final boolean importAll = is.readBoolean();
            final List<String> symbols = readStrings(is);
            final List<String> aliases = readStrings(is);

            CModule.Import im = new CModule.Import(name, importAll);
            im.getSymbols().addAll(symbols);
            im.getAliases().addAll(aliases);
            imports.add(im);
        }

        return imports;
    }

    protected BinaryNamespace readNameSpace(DataInputStream is, CModule module) throws IOException, BinaryFormatException {

        final int count = is.readInt();
        checkBufferSize(count);

        BinaryNamespace namespace = new BinaryNamespace(count);

        for(int i = 0; i < count; i++){
            final String name = is.readUTF();
            final byte flags = is.readByte();

            namespace.getEntries().add(new BinaryNamespace.Entry(name, flags, readObject(is, module)));
        }

        return namespace;
    }

    protected Object readObject(DataInputStream is, CModule module) throws IOException, BinaryFormatException {
        final int typeOrdinal = is.readByte();
        try {
            BinaryConstants.ConstantType type = BinaryConstants.ConstantType.values()[typeOrdinal];

            return switch (type) {
                case NULL -> CNull.instance();
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

    protected CMethod readMethod(DataInputStream is, CModule module) throws IOException, BinaryFormatException {
        CMethod method = new CMethod();

        CMethodCode code = new CMethodCode();
        code.setConstantPool(module.getConstants().toArray());

        code.setDebugSymbol(is.readUTF());
        code.setLocalCount(is.readInt());
        code.setArgCount(is.readInt());
        code.setDefaultArgCount(is.readInt());

        final int codeSize = is.readInt();
        checkBufferSize(codeSize);

        code.setCode(is.readNBytes(codeSize));
        code.setExceptionTable(readExceptionTable(is));
        code.setDebugTable(readDebugTable(is));

        method.setCode(code);

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

    protected CClass readClass(DataInputStream is, CModule module) throws IOException, BinaryFormatException {

        final String name = is.readUTF();
        CClass cls = new CClass(name, module);

        cls.setSharedInitializer(readMethod(is, module));

        BinaryNamespace sharedNamespace = readNameSpace(is, module);
        for(BinaryNamespace.Entry entry : sharedNamespace.getEntries()){
            final String symbolName = entry.getName();
            final int flags = entry.getFlags();

            cls.getAttributes().set(symbolName, entry.getValue());

            if(entry.getValue() instanceof CMethod){
                ((CMethod) entry.getValue()).bind(module);
            }

            if((flags & BinaryConstants.FINAL_FLAG) != 0){
                cls.getAttributes().markFinal(symbolName);
            }
        }

        cls.setInstanceInitializer(readMethod(is, module));

        BinaryNamespace namespace = readNameSpace(is, module);
        for(BinaryNamespace.Entry entry : namespace.getEntries()){
            final String symbolName = entry.getName();
            final int flags = entry.getFlags();

            cls.getAttributes().set(symbolName, entry.getValue());

            if((flags & BinaryConstants.FINAL_FLAG) != 0){
                cls.getAttributes().markFinal(symbolName);
            }

            if((flags & BinaryConstants.TRAIT_FLAG) != 0){
                cls.getAttributes().markTrait(symbolName);
            }
        }

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

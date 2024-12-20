/*
 * Copyright (C) 2024 MyWorld, LLC
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

package chipmunk.vm.hvm;

import chipmunk.binary.BinaryModule;
import chipmunk.compiler.assembler.HVMType;
import chipmunk.compiler.assembler.Operand;
import chipmunk.compiler.assembler.Operands;
import chipmunk.runtime.ChipmunkModule;
import myworld.hummingbird.Executable;
import myworld.hummingbird.Opcodes;

import java.nio.charset.StandardCharsets;

import static chipmunk.compiler.assembler.HVMType.*;
import static chipmunk.compiler.assembler.Opcodes.*;

public class HvmCompiler {

    public ChipmunkModule compileModule(BinaryModule module){
        var builder = Executable.builder();
        var operands = new Operands();

        // For now assume we have a single method with no arguments
        module.getNamespace().getEntries().forEach(e -> {
            System.out.println(e.getType() + " " + e.getName());
        });

        var method = module.getNamespace().getEntries().get(2).getBinaryMethod();
        var constants = method.getConstantPool();
        var code = method.getCode();
        var ip = 0;
        while(ip < code.length){
            switch (code[ip]){
                case PUSH -> ip = push(builder, constants, operands, code, ip);
                case ADD -> ip = add(builder, constants, operands, code, ip);
                case RETURN -> ip = _return(builder, constants, operands, code, ip);
            }
        }

        return new HvmModule(builder.build());
    }

    private int push(Executable.Builder builder, Object[] constants, Operands operands, byte[] code, int ip){
        var value = constants[fetchInt(code, ip + 1)];

        Operand op = null;
        if(value instanceof Integer || value instanceof Long){
            op = operands.push(LONG);
        }else if(value instanceof Float || value instanceof Double){
            op = operands.push(DOUBLE);
        }else if(value instanceof String){
            op = operands.push(STRING_REF);
        }else if(value == null){
            op = operands.push(LONG);
            value = 0L;
        }else{
            throw new IllegalArgumentException("Illegal constant type: " + value);
        }

        if(op.type() == HVMType.STRING_REF){
            // Strings have to be encoded in the data section, with the address pushed as a constant.
            value = builder.appendData(value.toString().getBytes(StandardCharsets.UTF_8));
        }
        builder.appendOpcode(Opcodes.CONST(op.register(), value));

        return ip + 5;
    }

    private int add(Executable.Builder builder, Object[] constants, Operands operands, byte[] code, int ip){
        var b = operands.pop();
        var a = operands.pop();

        if(a != b){
            ip = promoteType(builder, a, b, ip);
        }

        switch (a.type()){
            case LONG -> {
                builder.appendOpcode(Opcodes.ADD(a.register(), a.register(), b.register()));
                operands.push(LONG);
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.DADD(a.register(), a.register(), b.register()));
                operands.push(DOUBLE);
            }
        }

        return ip + 1;
    }

    private int _return(Executable.Builder builder, Object[] constants, Operands operands, byte[] code, int ip){
        builder.appendOpcode(Opcodes.RETURN(operands.pop().register()));
        return ip + 1;
    }

    protected int promoteType(Executable.Builder builder, Operand a, Operand b, int ip){
        if(a.type() == LONG && b.type() == DOUBLE){
            // Promote register with a
            builder.appendOpcode(Opcodes.L2D(a.register(), a.register()));
            return ip + 1;
        }else if(a.type() == DOUBLE && b.type() == LONG){
            // Promote register with b
            builder.appendOpcode(Opcodes.L2D(b.register(), b.register()));
            return ip + 1;
        }
        return ip;
    }

    private int fetchInt(byte[] instructions, int ip) {
        int b1 = instructions[ip] & 0xFF;
        int b2 = instructions[ip + 1] & 0xFF;
        int b3 = instructions[ip + 2] & 0xFF;
        int b4 = instructions[ip + 3] & 0xFF;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

}

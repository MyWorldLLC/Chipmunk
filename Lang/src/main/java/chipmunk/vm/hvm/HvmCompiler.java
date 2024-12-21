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
                case ADD -> ip = add(builder, operands, ip);
                case MUL -> ip = mul(builder, operands, ip);
                case DIV -> ip = div(builder, operands, ip);
                case FDIV -> ip = fdiv(builder, operands, ip);
                case MOD -> ip = mod(builder, operands, ip);
                case POW -> ip = pow(builder, operands, ip);
                case INC -> ip = inc(builder, operands, ip);
                case DEC -> ip = dec(builder, operands, ip);
                case POS -> ip = pos(builder, operands, ip);
                case NEG -> ip = neg(builder, operands, ip);
                case RETURN -> ip = _return(builder, operands, ip);
                default -> {
                    throw new IllegalArgumentException("Unknown opcode 0x%02X".formatted(code[ip]));
                }
            }
        }

        return new HvmModule(builder.build());
    }

    private int push(Executable.Builder builder, Object[] constants, Operands operands, byte[] code, int ip){
        var value = constants[fetchInt(code, ip + 1)];

        Operand op;
        if(value instanceof Integer || value instanceof Long){
            op = operands.push(LONG);
        }else if(value instanceof Float f){
            value = (double) f;
            op = operands.push(DOUBLE);
        }else if(value instanceof Double){
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

    private int add(Executable.Builder builder, Operands operands, int ip){
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        switch (wider(a, b)){
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

    private int mul(Executable.Builder builder, Operands operands, int ip){
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        switch (wider(a, b)){
            case LONG -> {
                builder.appendOpcode(Opcodes.MUL(a.register(), a.register(), b.register()));
                operands.push(LONG);
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.DMUL(a.register(), a.register(), b.register()));
                operands.push(DOUBLE);
            }
        }

        return ip + 1;
    }

    private int div(Executable.Builder builder, Operands operands, int ip){
        var b = operands.pop();
        var a = operands.pop();

        toDouble(builder, b);
        toDouble(builder, a);

        builder.appendOpcode(Opcodes.DDIV(a.register(), a.register(), b.register()));
        operands.push(DOUBLE);

        return ip + 1;
    }

    private int fdiv(Executable.Builder builder, Operands operands, int ip){
        var b = operands.pop();
        var a = operands.pop();

        toLong(builder, b);
        toLong(builder, a);

        builder.appendOpcode(Opcodes.DIV(a.register(), a.register(), b.register()));
        operands.push(LONG);

        return ip + 1;
    }

    private int mod(Executable.Builder builder, Operands operands, int ip){
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        switch (wider(a, b)){
            case LONG -> {
                builder.appendOpcode(Opcodes.REM(a.register(), a.register(), b.register()));
                operands.push(LONG);
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.DREM(a.register(), a.register(), b.register()));
                operands.push(DOUBLE);
            }
        }

        return ip + 1;
    }

    private int pow(Executable.Builder builder, Operands operands, int ip){
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        switch (wider(a, b)){
            case LONG -> {
                builder.appendOpcode(Opcodes.POW(a.register(), a.register(), b.register()));
                operands.push(LONG);
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.DPOW(a.register(), a.register(), b.register()));
                operands.push(DOUBLE);
            }
        }

        return ip + 1;
    }

    private int inc(Executable.Builder builder, Operands operands, int ip){
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                builder.appendOpcode(Opcodes.CADD(a.register(), a.register(), 1L));
                operands.push(LONG);
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.DCADD(a.register(), a.register(), 1.0d));
                operands.push(DOUBLE);
            }
        }

        return ip + 1;
    }

    private int dec(Executable.Builder builder, Operands operands, int ip){
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                builder.appendOpcode(Opcodes.CADD(a.register(), a.register(), -1L));
                operands.push(LONG);
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.DCADD(a.register(), a.register(), -1.0d));
                operands.push(DOUBLE);
            }
        }

        return ip + 1;
    }

    private int pos(Executable.Builder builder, Operands operands, int ip){
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                builder.appendOpcode(Opcodes.ABS(a.register(), a.register()));
                operands.push(LONG);
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.DABS(a.register(), a.register()));
                operands.push(DOUBLE);
            }
        }

        return ip + 1;
    }

    private int neg(Executable.Builder builder, Operands operands, int ip){
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                builder.appendOpcode(Opcodes.NEG(a.register(), a.register()));
                operands.push(LONG);
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.DNEG(a.register(), a.register()));
                operands.push(DOUBLE);
            }
        }

        return ip + 1;
    }

    private int _return(Executable.Builder builder, Operands operands, int ip){
        var op = operands.pop();
        builder.appendOpcode(Opcodes.RETURN(op.register()));
        return ip + 1;
    }

    protected void promoteNarrower(Executable.Builder builder, Operand a, Operand b){
        if(a.type() == LONG && b.type() == DOUBLE){
            // Promote register with a
            builder.appendOpcode(Opcodes.L2D(a.register(), a.register()));
        }else if(a.type() == DOUBLE && b.type() == LONG){
            // Promote register with b
            builder.appendOpcode(Opcodes.L2D(b.register(), b.register()));
        }
    }

    protected void toDouble(Executable.Builder builder, Operand a){
        if(a.type() == LONG){
            builder.appendOpcode(Opcodes.L2D(a.register(), a.register()));
        }
    }

    protected void toLong(Executable.Builder builder, Operand a){
        if(a.type() == DOUBLE){
            builder.appendOpcode(Opcodes.D2L(a.register(), a.register()));
        }
    }

    protected HVMType wider(Operand a, Operand b){
        if(a.type() == DOUBLE || b.type() == DOUBLE){
            return DOUBLE;
        }
        return LONG;
    }

    private int fetchInt(byte[] instructions, int ip) {
        int b1 = instructions[ip] & 0xFF;
        int b2 = instructions[ip + 1] & 0xFF;
        int b3 = instructions[ip + 2] & 0xFF;
        int b4 = instructions[ip + 3] & 0xFF;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

}

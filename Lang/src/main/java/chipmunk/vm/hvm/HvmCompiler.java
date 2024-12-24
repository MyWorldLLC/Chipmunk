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

        // For now assume we have a single method with no arguments
        module.getNamespace().getEntries().forEach(e -> {
            System.out.println(e.getType() + " " + e.getName());
        });

        var method = module.getNamespace().getEntries().get(2).getBinaryMethod();
        var builder = new HvmCodeBuilder(method);

        var constants = method.getConstantPool();
        var code = method.getCode();

        // TODO - have to handle ip remapping between Chipmunk & HVM bytecode. Chipmunk
        // uses the raw ip, which means that one instruction could take 1, 5, or more bytes.
        // HVM uses the instruction index, which means every value of ip points to a specific
        // instruction. Multi-byte Chipmunk instructions mean that we can't use the raw jump
        // targets Chipmunk specifies - we have to re-calculate them after the Chipmunk bytecode
        // has been decoded.

        while(builder.hasNextOp()){
            var op = builder.nextOp();
            switch (op){
                // Arithmetic
                case ADD -> add(builder);
                case MUL -> mul(builder);
                case DIV -> div(builder);
                case FDIV -> fdiv(builder);
                case MOD -> mod(builder);
                case POW -> pow(builder);
                case INC -> inc(builder);
                case DEC -> dec(builder);
                case POS -> pos(builder);
                case NEG -> neg(builder);

                // Bitwise
                case BXOR -> bxor(builder);
                case BAND -> band(builder);
                case BOR -> bor(builder);
                case BNEG -> bneg(builder);
                case LSHIFT -> lshift(builder);
                case RSHIFT -> rshift(builder);
                case URSHIFT -> urshift(builder);

                // Stack
                // Note: swap not implemented since it is currently unused by the assembler
                case POP -> builder.operands().pop();
                case DUP -> builder.operands().dup();
                case PUSH -> push(builder);

                // Locals
                case GETLOCAL -> getlocal(builder);
                case SETLOCAL -> setlocal(builder);

                // Flow control
                case IF -> _if(builder);
                case GOTO -> _goto(builder);
                case RETURN -> _return(builder);

                // Comparison/Boolean operations
                case NOT -> not(builder);
                case EQ -> eq(builder);

                // Object operations
                case TRUTH -> truth(builder);
                default -> {
                    throw new IllegalArgumentException("Unknown opcode 0x%02X".formatted(op));
                }
            }
        }

        return new HvmModule(builder.build());
    }

    private void push(HvmCodeBuilder builder){
        var operands = builder.operands();
        var value = builder.constant(builder.fetchInt());

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
        }else if(value instanceof Boolean b){
            value = b ? 1L : 0L;
            op = operands.push(LONG);
        }
        else if(value == null){
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
    }

    private void add(HvmCodeBuilder builder){
        var operands = builder.operands();
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
    }

    private void mul(HvmCodeBuilder builder){
        var operands = builder.operands();
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
    }

    private void div(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        toDouble(builder, b);
        toDouble(builder, a);

        builder.appendOpcode(Opcodes.DDIV(a.register(), a.register(), b.register()));
        operands.push(DOUBLE);
    }

    private void fdiv(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        toLong(builder, b);
        toLong(builder, a);

        builder.appendOpcode(Opcodes.DIV(a.register(), a.register(), b.register()));
        operands.push(LONG);
    }

    private void mod(HvmCodeBuilder builder){
        var operands = builder.operands();
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
    }

    private void pow(HvmCodeBuilder builder){
        var operands = builder.operands();
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
    }

    private void inc(HvmCodeBuilder builder){
        var operands = builder.operands();
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
    }

    private void dec(HvmCodeBuilder builder){
        var operands = builder.operands();
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
    }

    private void pos(HvmCodeBuilder builder){
        var operands = builder.operands();
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
    }

    private void neg(HvmCodeBuilder builder){
        var operands = builder.operands();
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
    }

    private void bxor(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        builder.appendOpcode(Opcodes.BXOR(a.register(), a.register(), b.register()));
        operands.push(LONG);
    }

    private void band(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        builder.appendOpcode(Opcodes.BAND(a.register(), a.register(), b.register()));
        operands.push(LONG);
    }

    private void bor(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        builder.appendOpcode(Opcodes.BOR(a.register(), a.register(), b.register()));
        operands.push(LONG);
    }

    private void bneg(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        builder.appendOpcode(Opcodes.BNOT(a.register(), a.register()));
        operands.push(LONG);
    }

    private void lshift(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        builder.appendOpcode(Opcodes.BLSHIFT(a.register(), a.register(), b.register()));
        operands.push(LONG);
    }

    private void rshift(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        builder.appendOpcode(Opcodes.BSRSHIFT(a.register(), a.register(), b.register()));
        operands.push(LONG);
    }

    private void urshift(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        builder.appendOpcode(Opcodes.BURSHIFT(a.register(), a.register(), b.register()));
        operands.push(LONG);
    }

    private void getlocal(HvmCodeBuilder builder){
        var operands = builder.operands();
        var registerStates = builder.registerStates();

        var index = builder.fetchInt();

        var type = registerStates.getLocal(index);
        if(type == null){
            // Default locals to long if they're not initialized, though in theory this should never happen,
            // and we probably should throw here instead
            type = LONG;
        }

        var target = operands.push(type);
        builder.appendOpcode(Opcodes.COPY(target.register(), index));
    }

    private void setlocal(HvmCodeBuilder builder){
        var operands = builder.operands();
        var registerStates = builder.registerStates();
        var index = builder.fetchInt();

        var source = operands.pop();
        registerStates.setLocal(index, source.type());

        builder.appendOpcode(Opcodes.COPY(index, source.register()));
    }

    private void _if(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        var target = builder.fetchInt();

        switch (a.type()){
            case LONG -> {
                builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1L));
                builder.deferOpcode(() -> Opcodes.IFNE(a.register(), a.register() + 1, builder.remapIp(target)));
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1d));
                builder.deferOpcode(() -> Opcodes.DIFNE(a.register(), a.register() + 1, builder.remapIp(target)));
            }
        }
    }

    private void _goto(HvmCodeBuilder builder){
        var target = builder.fetchInt();
        builder.deferOpcode(() ->
           Opcodes.GOTO(builder.remapIp(target))
        );
    }

    private void  _return(HvmCodeBuilder builder){
        var operands = builder.operands();
        var op = operands.pop();
        builder.appendOpcode(Opcodes.RETURN(op.register()));
    }

    private void not(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        // Assume value is a 1/0 boolean & flip the truth bit
        builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1L));
        builder.appendOpcode(Opcodes.BXOR(a.register(), a.register(), a.register() + 1));
        operands.push(LONG);
    }

    private void eq(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        if(builder.peekNextOp() == IF){
            var target = builder.fetchInt();
            // Fuse to IFEQ
            switch (wider(a, b)){
                case LONG -> builder.appendOpcode(Opcodes.IFEQ(a.register(), b.register(), target));
                case DOUBLE -> builder.appendOpcode(Opcodes.DIFEQ(a.register(), b.register(), target));
            }
        }else{
            // TODO - emit boolean result since this isn't a fused operation
        }
    }

    private void truth(HvmCodeBuilder builder){
        // TODO - probably needs to be changed
        var operands = builder.operands();
        var a = operands.pop();
        builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1L));
        builder.appendOpcode(Opcodes.BAND(a.register(), a.register(), a.register() + 1));
        operands.push(LONG);
    }

    protected void promoteNarrower(HvmCodeBuilder builder, Operand a, Operand b){
        if(a.type() == LONG && b.type() == DOUBLE){
            // Promote register with a
            builder.appendOpcode(Opcodes.L2D(a.register(), a.register()));
        }else if(a.type() == DOUBLE && b.type() == LONG){
            // Promote register with b
            builder.appendOpcode(Opcodes.L2D(b.register(), b.register()));
        }
    }

    protected void toDouble(HvmCodeBuilder builder, Operand a){
        if(a.type() == LONG){
            builder.appendOpcode(Opcodes.L2D(a.register(), a.register()));
        }
    }

    protected void toLong(HvmCodeBuilder builder, Operand a){
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

}

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
import chipmunk.runtime.ChipmunkModule;
import myworld.hummingbird.Opcodes;

import java.nio.charset.StandardCharsets;

import static chipmunk.compiler.assembler.HVMType.*;
import static chipmunk.compiler.assembler.Opcodes.*;

public class HvmCompiler {

    public ChipmunkModule compileModule(BinaryModule module){

        // For now assume we have a single method with no arguments
        // evaluate() will be the third method for eval() calls
        var method = module.getNamespace().getEntries().get(2).getBinaryMethod();
        var builder = new HvmCodeBuilder(method);

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
                case LT -> lt(builder);
                case LE -> le(builder);

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
        builder.appendOpcode(Opcodes.CONST(builder.getTos(), value));
    }

    private void add(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        switch (wider(a, b)){
            case LONG -> {
                operands.push(LONG);
                builder.appendOpcode(Opcodes.ADD(builder.getTos(), a.register(), b.register()));
            }
            case DOUBLE -> {
                operands.push(DOUBLE);
                builder.appendOpcode(Opcodes.DADD(builder.getTos(), a.register(), b.register()));
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
                operands.push(LONG);
                builder.appendOpcode(Opcodes.MUL(builder.getTos(), a.register(), b.register()));
            }
            case DOUBLE -> {
                operands.push(DOUBLE);
                builder.appendOpcode(Opcodes.DMUL(builder.getTos(), a.register(), b.register()));
            }
        }
    }

    private void div(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        toDouble(builder, b);
        toDouble(builder, a);

        operands.push(DOUBLE);
        builder.appendOpcode(Opcodes.DDIV(builder.getTos(), a.register(), b.register()));

    }

    private void fdiv(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        toLong(builder, b);
        toLong(builder, a);

        operands.push(LONG);
        builder.appendOpcode(Opcodes.DIV(builder.getTos(), a.register(), b.register()));
    }

    private void mod(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        switch (wider(a, b)){
            case LONG -> {
                operands.push(LONG);
                builder.appendOpcode(Opcodes.REM(builder.getTos(), a.register(), b.register()));
            }
            case DOUBLE -> {
                operands.push(DOUBLE);
                builder.appendOpcode(Opcodes.DREM(builder.getTos(), a.register(), b.register()));

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
                operands.push(LONG);
                builder.appendOpcode(Opcodes.POW(builder.getTos(), a.register(), b.register()));
            }
            case DOUBLE -> {
                operands.push(DOUBLE);
                builder.appendOpcode(Opcodes.DPOW(builder.getTos(), a.register(), b.register()));
            }
        }
    }

    private void inc(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                operands.push(LONG);
                builder.appendOpcode(Opcodes.CADD(builder.getTos(), a.register(), 1L));
            }
            case DOUBLE -> {
                operands.push(DOUBLE);
                builder.appendOpcode(Opcodes.DCADD(builder.getTos(), a.register(), 1.0d));
            }
        }
    }

    private void dec(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                operands.push(LONG);
                builder.appendOpcode(Opcodes.CADD(builder.getTos(), a.register(), -1L));
            }
            case DOUBLE -> {
                operands.push(DOUBLE);
                builder.appendOpcode(Opcodes.DCADD(builder.getTos(), a.register(), -1.0d));
            }
        }
    }

    private void pos(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                operands.push(LONG);
                builder.appendOpcode(Opcodes.ABS(builder.getTos(), a.register()));
            }
            case DOUBLE -> {
                operands.push(DOUBLE);
                builder.appendOpcode(Opcodes.DABS(builder.getTos(), a.register()));
            }
        }
    }

    private void neg(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                operands.push(LONG);
                builder.appendOpcode(Opcodes.NEG(builder.getTos(), a.register()));
            }
            case DOUBLE -> {
                operands.push(DOUBLE);
                builder.appendOpcode(Opcodes.DNEG(builder.getTos(), a.register()));
            }
        }
    }

    private void bxor(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        operands.push(LONG);
        builder.appendOpcode(Opcodes.BXOR(builder.getTos(), a.register(), b.register()));
    }

    private void band(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        operands.push(LONG);
        builder.appendOpcode(Opcodes.BAND(builder.getTos(), a.register(), b.register()));
    }

    private void bor(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        operands.push(LONG);
        builder.appendOpcode(Opcodes.BOR(builder.getTos(), a.register(), b.register()));
    }

    private void bneg(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        operands.push(LONG);
        builder.appendOpcode(Opcodes.BNOT(builder.getTos(), a.register()));
    }

    private void lshift(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        operands.push(LONG);
        builder.appendOpcode(Opcodes.BLSHIFT(builder.getTos(), a.register(), b.register()));
    }

    private void rshift(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        operands.push(LONG);
        builder.appendOpcode(Opcodes.BSRSHIFT(builder.getTos(), a.register(), b.register()));
    }

    private void urshift(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        // Disregard operand types - treat everything as a long
        operands.push(LONG);
        builder.appendOpcode(Opcodes.BURSHIFT(builder.getTos(), a.register(), b.register()));
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

        operands.push(type);
        builder.appendOpcode(Opcodes.COPY(builder.getTos(), index));
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
                builder.markJump(target);
                builder.deferOpcode(() -> Opcodes.IFNE(a.register(), a.register() + 1, builder.remapIp(target)));
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1d));
                builder.markJump(target);
                builder.deferOpcode(() -> Opcodes.DIFNE(a.register(), a.register() + 1, builder.remapIp(target)));
            }
        }
    }

    private void _goto(HvmCodeBuilder builder){
        var target = builder.fetchInt();
        builder.markJump(target);
        builder.deferOpcode(() ->
            Opcodes.GOTO(builder.remapIp(target))
        );
    }

    private void _return(HvmCodeBuilder builder){
        var operands = builder.operands();
        var op = operands.pop();
        System.out.println("Return @%d".formatted(builder.builder.indexOfNextOpcode()));
        //builder.appendOpcode(Opcodes.DEBUG(op.register(), op.register()));
        builder.appendOpcode(Opcodes.RETURN(op.register()));
    }

    private void not(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();

        // Assume value is a 1/0 boolean & flip the truth bit
        operands.push(LONG);
        var tos = builder.getTos();
        builder.appendOpcode(Opcodes.CONST(tos + 1, 1L));
        builder.appendOpcode(Opcodes.BXOR(tos, a.register(), tos + 1));
    }

    private void eq(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        if(builder.peekNextOp() == IF){
            var target = builder.fetchInt();
            // Fuse to IFNE
            switch (wider(a, b)){
                case LONG -> {
                    builder.markJump(target);
                    builder.deferOpcode(() -> Opcodes.IFNE(a.register(), b.register(), builder.remapIp(target)));
                }
                case DOUBLE -> {
                    builder.markJump(target);
                    builder.deferOpcode(() -> Opcodes.DIFNE(a.register(), b.register(), builder.remapIp(target)));
                }
            }
        }else{
            switch (wider(a, b)){
                case LONG -> {
                    operands.push(LONG);
                    builder.appendOpcode(Opcodes.TIFEQ(builder.getTos(), b.register(), a.register()));
                }
                case DOUBLE -> {
                    operands.push(LONG);
                    builder.appendOpcode(Opcodes.TDIFEQ(builder.getTos(), b.register(), a.register()));
                }
            }
        }
    }

    private void lt(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        if(builder.peekNextOp() == IF){
            var target = builder.fetchInt();
            // Fuse to IFLT
            switch (wider(a, b)){
                case LONG -> {
                    builder.markJump(target);
                    builder.deferOpcode(() -> Opcodes.IFLT(a.register(), b.register(), builder.remapIp(target)));
                }
                case DOUBLE -> {
                    builder.markJump(target);
                    builder.deferOpcode(() -> Opcodes.DIFLT(a.register(), b.register(), builder.remapIp(target)));
                }
            }
        }else{
            switch (wider(a, b)){
                case LONG -> {
                    operands.push(LONG);
                    builder.appendOpcode(Opcodes.TIFLT(builder.getTos(), b.register(), a.register()));
                }
                case DOUBLE -> {
                    operands.push(LONG);
                    builder.appendOpcode(Opcodes.TDIFLT(builder.getTos(), b.register(), a.register()));
                }
            }
        }
    }

    private void le(HvmCodeBuilder builder){
        var operands = builder.operands();
        var b = operands.pop();
        var a = operands.pop();

        promoteNarrower(builder, a, b);

        if(builder.peekNextOp() == IF){
            var target = builder.fetchInt();
            // Fuse to IFLE
            switch (wider(a, b)){
                case LONG -> {
                    builder.markJump(target);
                    builder.deferOpcode(() -> Opcodes.IFLE(a.register(), b.register(), builder.remapIp(target)));
                }
                case DOUBLE -> {
                    builder.markJump(target);
                    builder.deferOpcode(() -> Opcodes.DIFLE(a.register(), b.register(), builder.remapIp(target)));
                }
            }
        }else{
            switch (wider(a, b)){
                case LONG -> {
                    operands.push(LONG);
                    builder.appendOpcode(Opcodes.TIFLE(builder.getTos(), b.register(), a.register()));
                }
                case DOUBLE -> {
                    operands.push(LONG);
                    builder.appendOpcode(Opcodes.TDIFLE(builder.getTos(), b.register(), a.register()));
                }
            }
        }
    }

    private void truth(HvmCodeBuilder builder){
        var operands = builder.operands();
        var a = operands.pop();
        operands.push(LONG);
        var tos = builder.getTos();
        builder.appendOpcode(Opcodes.CONST(tos + 1, 1L));
        builder.appendOpcode(Opcodes.BAND(tos, a.register(), tos + 1));
        builder.appendOpcode(Opcodes.DEBUG(tos, a.register()));
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

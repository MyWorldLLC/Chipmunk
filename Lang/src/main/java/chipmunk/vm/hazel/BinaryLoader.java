/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.vm.hazel;

import chipmunk.binary.BinaryModule;
import chipmunk.binary.BinaryNamespace;
import chipmunk.binary.FieldType;
import chipmunk.runtime.CClass;
import chipmunk.runtime.CField;
import chipmunk.runtime.CMethod;
import chipmunk.runtime.CModule;
import chipmunk.vm.hazel.instructions.*;

import java.util.ArrayList;
import java.util.Arrays;

import static chipmunk.vm.Opcodes.*;

public class BinaryLoader {

    public static CModule loadModule(BinaryModule module){
        var namespace = module.getNamespace();
        var cModule = new CModule(module.getName(), module.getFileName());
        cModule.setConstantPool(module.getConstantPool());
        cModule.setFields(collectFields(namespace));
        cModule.setMethods(collectMethods(cModule, namespace));
        cModule.setClasses(collectClasses(cModule, namespace));
        return cModule;
    }

    public static CField[] collectFields(BinaryNamespace namespace){
        return namespace.getEntries().stream()
                .filter(e -> e.getType() == FieldType.DYNAMIC_VAR)
                .map(BinaryLoader::entryField)
                .toArray(CField[]::new);
    }

    public static CField entryField(BinaryNamespace.Entry entry){
        return new CField(entry.getName(), entry.getFlags());
    }

    public static CClass[] collectClasses(CModule module, BinaryNamespace namespace){
        return namespace.getEntries().stream()
                .filter(e -> e.getType() == FieldType.CLASS)
                .map(e -> BinaryLoader.entryClass(module, e))
                .toArray(CClass[]::new);
    }

    public static CClass entryClass(CModule module, BinaryNamespace.Entry entry){
        // TODO - visit instance namespace
        // TODO - visit shared namespace
        return null; // TODO
    }

    public static CMethod[] collectMethods(CModule module, BinaryNamespace namespace){
        return namespace.getEntries().stream()
                .filter(e -> e.getType() == FieldType.METHOD)
                .map(e -> entryMethod(module, e))
                .toArray(CMethod[]::new);
    }

    public static CMethod entryMethod(CModule module, BinaryNamespace.Entry entry){
        var binaryMethod = entry.getBinaryMethod();
        var code = binaryMethod.getCode();
        final var argCount = binaryMethod.getArgCount();
        final var localCount = binaryMethod.getLocalCount();

        // This maps every IP in the code array that contains an opcode to an
        // instruction index. This is used to remap jump targets from binary code indices
        // to logical instruction indices.
        var remapping = new int[code.length];

        // This tracks the stack depth at each instruction. This is longer than needed since it
        // is allocated with the length of the bytecode rather than the length of the logical instructions,
        // but bytecode is fairly dense so this isn't a big problem. Indices are made with instruction pointers,
        // not raw bytecode pointers.
        var stackDepths = new int[code.length];

        // For jump retargeting, a second pass is needed. This list
        // holds runnables that will be applied after the initial translation
        // pass is complete. Each one is intended to replace a single instruction.
        var postProcessors = new ArrayList<Runnable>();
        var instructions = new ArrayList<Instruction>();

        // SP - the "stack pointer." This always references the index on the stack (relative to this method's frame)
        // where the TOS value is. This means that "stack growing" ops (such as PUSH) write to SP + 1, and "stack shrinking"
        // ops (such as ADD) read TOS, TOS - 1, and leave their result at TOS - N (where N = the arity of the operator - 1).

        int ip = 0;
        // Stack pointer - used to track the stack offsets each instruction operates on. Since the stack is initially empty
        // this points to the index of the last local variable (which is an invalid stack index).
        int sp = localCount - 1;
        while(ip < code.length){
            var op = code[ip];
            // We can't just do a simple increment because pop doesn't emit a runtime instruction, conditional fusing
            // merges multiple bytecodes into a single dispatch instruction, etc.
            var instruction = instructions.size();
            remapping[ip] = instruction;
            stackDepths[instruction] = sp;
            switch(op){
                case ADD -> {
                    instructions.add(new Add(sp));
                    sp--;
                    ip++;
                }
                case SUB -> {
                    instructions.add(new Sub(sp));
                    sp--;
                    ip++;
                }
                case MUL -> {
                    instructions.add(new Mul(sp));
                    sp--;
                    ip++;
                }
                case DIV -> {
                    instructions.add(new Div(sp));
                    sp--;
                    ip++;
                }
                case FDIV -> {
                    instructions.add(new FDiv(sp));
                    sp--;
                    ip++;
                }
                case MOD -> {
                    instructions.add(new Mod(sp));
                    sp--;
                    ip++;
                }
                case POW -> {
                    instructions.add(new Pow(sp));
                    sp--;
                    ip++;
                }
                case INC -> {
                    instructions.add(new Inc(sp));
                    ip++;
                }
                case DEC -> {
                    instructions.add(new Dec(sp));
                    ip++;
                }
                case POS -> {
                    instructions.add(new Pos(sp));
                    ip++;
                }
                case NEG -> {
                    instructions.add(new Neg(sp));
                    ip++;
                }
                case BXOR -> {
                    instructions.add(new Bxor(sp));
                    sp--;
                    ip++;
                }
                case BAND -> {
                    instructions.add(new Band(sp));
                    sp--;
                    ip++;
                }
                case BOR -> {
                    instructions.add(new Bor(sp));
                    sp--;
                    ip++;
                }
                case BNEG -> {
                    instructions.add(new BNeg(sp));
                    ip++;
                }
                case LSHIFT -> {
                    instructions.add(new LShift(sp));
                    sp--;
                    ip++;
                }
                case RSHIFT -> {
                    instructions.add(new RShift(sp));
                    sp--;
                    ip++;
                }
                case URSHIFT -> {
                    instructions.add(new URShift(sp));
                    sp--;
                    ip++;
                }
                case PUSH -> {
                    instructions.add(new Push(sp, fetchInt(code, ip + 1)));
                    sp++;
                    ip += 5;
                }
                case POP -> {
                    // Pop is a no-op at runtime. Since stack depth is encoded in the instruction rather than tracked
                    // at runtime we don't need to actually do anything for a pop. In the worst case, we might
                    // leave a collectible pointer on the stack for slightly longer than it needs to be there.
                    sp--;
                    ip++;
                }
                case DUP -> {
                    instructions.add(new Dup(sp));
                    sp++;
                    ip++;
                }
                case SWAP -> {
                    instructions.add(new Swap(sp));
                    ip++;
                }
                case GETLOCAL -> {
                    instructions.add(new LocalGet(sp, code[ip + 1]));
                    sp++;
                    ip += 2;
                }
                case SETLOCAL -> {
                    instructions.add(new LocalSet(sp, code[ip + 1]));
                    sp--;
                    ip += 2;
                }
                case GOTO -> {
                    var target = fetchInt(code, ip + 1);
                    var replace = instruction;
                    instructions.add(null);
                    postProcessors.add(() -> {
                        var jumpTo = remapping[target];
                        if(jumpTo < replace){
                            jumpTo = -jumpTo;
                        }
                        instructions.set(replace, new Goto(stackDepths[remapping[target]], jumpTo));
                    });
                    ip += 5;
                }
                case RETURN -> {
                    instructions.add(new Return(sp));
                    ip++;
                }
                case LT, GT, LE, GE, EQ, IS, INSTANCEOF -> {
                    // Attempt to fuse COND -> IF sequences in the raw bytecode for dispatch efficiency.
                    var jump = code[ip + 1] == IF;
                    var target = jump ? fetchInt(code, ip + 2) : Integer.MIN_VALUE;
                    var condition = switch (op){
                        case LT -> BinaryCondition.COND_LT;
                        case GT -> BinaryCondition.COND_GT;
                        case LE -> BinaryCondition.COND_LE;
                        case GE -> BinaryCondition.COND_GE;
                        case EQ -> BinaryCondition.COND_EQ;
                        case IS -> BinaryCondition.COND_IS;
                        case INSTANCEOF -> BinaryCondition.COND_INSTANCEOF;
                        default -> 0; // This should never be possible to hit, need it here to keep the compiler happy
                    };
                    var replace = instruction;
                    instructions.add(null);
                    postProcessors.add(() -> {
                        var jumpTo = remapping[target];
                        if(jumpTo < replace){
                            jumpTo = -jumpTo;
                        }
                        instructions.set(replace, new BinaryCondition(stackDepths[remapping[target]], condition, jumpTo));
                    });
                    sp--;
                    ip += jump ? 6 : 1;
                }
                case TRUTH, NOT -> {
                    // Attempt to fuse COND -> IF sequences in the raw bytecode for dispatch efficiency.
                    var jump = code[ip + 1] == IF;
                    var target = jump ? fetchInt(code, ip + 2) : Integer.MIN_VALUE;
                    var condition = switch (op){
                        case TRUTH -> UnaryCondition.COND_TRUE;
                        case NOT -> UnaryCondition.COND_NOT;
                        default -> 0; // This should never be possible to hit, need it here to keep the compiler happy
                    };
                    var replace = instruction;
                    instructions.add(null);
                    postProcessors.add(() -> {
                        var jumpTo = remapping[target];
                        if(jumpTo < replace){
                            jumpTo = -jumpTo;
                        }
                        instructions.set(replace, new UnaryCondition(stackDepths[remapping[target]], condition, jumpTo));
                    });
                    sp--;
                    ip += jump ? 6 : 1;
                }
                case IF -> {
                    var target = fetchInt(code, ip + 1);
                    var replace = instruction;
                    instructions.add(null);
                    postProcessors.add(() -> {
                        var jumpTo = remapping[target];
                        if(jumpTo < replace){
                            jumpTo = -jumpTo;
                        }
                        instructions.set(replace, new If(stackDepths[remapping[target]], jumpTo));
                    });
                    ip += 5;
                }
                case CALL -> {
                    instructions.add(new Call(sp, "call", code[ip + 1]));
                    ip += 2;
                }
                case CALLAT -> {
                    var name = (String) binaryMethod.getConstantPool()[fetchInt(code, ip + 2)];
                    instructions.add(new Call(sp, name, code[ip + 1]));
                    ip += 6;
                }
                case THROW -> {
                    instructions.add(new Throw(sp));
                    sp--;
                    ip++;
                    ip += 1;
                }
                case SETATTR -> {
                    ip += 5; // TODO
                }
                case GETAT -> {

                }
                case SETAT -> {

                }
                case AS -> {

                }
                case ITER -> {

                }
                case RANGE -> {

                }
                case LIST -> {

                }
                case MAP -> {

                }
                case INITUPVALUE -> {

                }
                case GETUPVALUE -> {

                }
                case SETUPVALUE -> {

                }
                case BIND -> {

                }
                default -> throw new IllegalArgumentException("Invalid opcode: 0x%2X".formatted(op));
            }

        }

        postProcessors.forEach(Runnable::run);

        // TODO - Remap exception handling table also
        var maxStack = Arrays.stream(stackDepths).max().getAsInt() + 1;

        var method = new CMethod(module, entry.getName(), instructions.toArray(Instruction[]::new), argCount, localCount, maxStack);
        method.setOriginalCode(code);
        return method;
    }

    public static int fetchInt(byte[] instructions, int ip) {
        int b1 = instructions[ip] & 0xFF;
        int b2 = instructions[ip + 1] & 0xFF;
        int b3 = instructions[ip + 2] & 0xFF;
        int b4 = instructions[ip + 3] & 0xFF;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }
}

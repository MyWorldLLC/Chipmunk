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

package chipmunk.jvm;

import chipmunk.InvalidOpcodeChipmunk;
import chipmunk.binary.*;
import chipmunk.invoke.Binder;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static chipmunk.Opcodes.*;

public class JvmCompiler {

    private final ChipmunkClassLoader loader;

    protected final CompiledMethods methods;

    public JvmCompiler(){
        loader = new ChipmunkClassLoader();
        methods = new CompiledMethods();
    }

    public CompiledModule compile(BinaryModule module){

        Type objType = Type.getType(Object.class);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, module.getName(), null, objType.getInternalName(), new String[]{Type.getType(CompiledModule.class).getInternalName()});

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, objType.getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        for(BinaryNamespace.Entry entry : module.getNamespace()){
            int flags = Opcodes.ACC_PUBLIC;
            if(BinaryConstants.isFlagSet(entry.getFlags(), BinaryConstants.FINAL_FLAG)){
                flags |= Opcodes.ACC_FINAL;
            }

            if(entry.getType() == FieldType.METHOD){
                visitMethod(cw, flags, entry.getName(), entry.getBinaryMethod());
            }else{
                visitVar(cw, flags, entry.getName());
            }
        }

        // TODO - generate module bytecode initializer method & make class implement the CompiledModule interface
        // The module initializer must run before anything else because it sets all Chipmunk bytecode fields
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "initializeCodeFields", Type.getMethodType(Type.VOID_TYPE, Type.getType(CompiledMethods.class)).getDescriptor(), null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitEnd();

        // TODO - generate the normal module initializer method
        // This is the usual module initializer that runs all module-level variable expressions & runs the
        // CClass shared initializers
        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        CompiledModule loadedModule = (CompiledModule) instantiate(loadClass(module.getName(), bytes));
        loadedModule.initializeCodeFields(methods);

        return loadedModule;
    }

    protected Class<?> loadClass(String name, byte[] bytes){
        return loader.define(name, bytes);
    }

    protected Object instantiate(Class<?> cClass){
        try {
            return cClass.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected void visitVar(ClassWriter cw, int flags, String name){
        visitVar(cw, flags, name, Type.getType(Object.class));
    }

    protected void visitVar(ClassWriter cw, int flags, String name, Type type){
        cw.visitField(flags, name, type.getDescriptor(), null, null).visitEnd();
    }

    protected void visitMethod(ClassWriter cw, int flags, String name, BinaryMethod method){

        final Type objType = Type.getType(Object.class);

        Type[] pTypes = new Type[method.getArgCount()];
        Arrays.fill(pTypes, objType);

        Type methodType = Type.getMethodType(objType, pTypes);

        // Generate a method of the form name(vm, params) that contains the bytecode for the expression
        // return vm.dispatch(receiver, Class.cm$name, params);
        MethodVisitor mv = cw.visitMethod(flags, name, methodType.getDescriptor(), null, null);
        mv.visitCode();
        //mv.visitVarInsn(Opcodes.ALOAD, 0);
        //mv.visitInsn(Opcodes.ARETURN);

        /*mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.GETSTATIC, className, "cm$" + name, Type.getType(BinaryMethod.class).getDescriptor());
        mv.visitVarInsn(Opcodes.ALOAD, 2);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkVM.class).getInternalName(),
                "dispatch",
                Type.getMethodType(Type.getType(Object.class), Type.getType(Object.class), Type.getType(BinaryMethod.class), Type.getType(Object[].class)).getDescriptor(),
                false);*/

        byte[] instructions = method.getCode();
        for(int ip = 0; ip < instructions.length;) {

            final byte op = instructions[ip];
            System.out.println(String.format("Op: %X", op));

            switch (op) {
                case ADD -> {
                    generateDynamicInvocation(mv, "plus", 1);
                    ip++;
                }
                case SUB -> {
                    generateDynamicInvocation(mv, "minus", 1);
                    ip++;
                }
                case MUL -> {
                    generateDynamicInvocation(mv, "mul", 1);
                    ip++;
                }
                case DIV -> {
                    generateDynamicInvocation(mv, "div", 1);
                    ip++;
                }
                case FDIV -> {
                    generateDynamicInvocation(mv, "fdiv", 1);
                    ip++;
                }
                case MOD -> {
                    generateDynamicInvocation(mv, "mod", 1);
                    ip++;
                }
                case POW -> {
                    generateDynamicInvocation(mv, "pow", 1);
                    ip++;
                }
                case INC -> {
                    generateDynamicInvocation(mv, "inc", 0);
                    ip++;
                }
                case DEC -> {
                    generateDynamicInvocation(mv, "dec", 0);
                    ip++;
                }
                case POS -> {
                    generateDynamicInvocation(mv, "pos", 0);
                    ip++;
                }
                case NEG -> {
                    generateDynamicInvocation(mv, "neg", 0);
                    ip++;
                }
                case AND -> {
                    generateDynamicInvocation(mv, "truth", 0);
                    generateDynamicInvocation(mv, "truth", 0);
                    generateBoxedAnd(cw, mv);
                    ip++;
                }
                case OR -> {
                    generateDynamicInvocation(mv, "truth", 0);
                    generateDynamicInvocation(mv, "truth", 0);
                    generateBoxedOr(cw, mv);
                    ip++;
                }
                case BXOR -> {
                    generateDynamicInvocation(mv, "binaryXor", 1);
                    ip++;
                }
                case BAND -> {
                    generateDynamicInvocation(mv, "binaryAnd", 1);
                    ip++;
                }
                case BOR -> {
                    generateDynamicInvocation(mv, "binaryOr", 1);
                    ip++;
                }
                case BNEG -> {
                    generateDynamicInvocation(mv, "binaryNeg", 1);
                    ip++;
                }
                case LSHIFT -> {
                    generateDynamicInvocation(mv, "lShift", 1);
                    ip++;
                }
                case RSHIFT -> {
                    generateDynamicInvocation(mv, "rShift", 1);
                    ip++;
                }
                case URSHIFT -> {
                    generateDynamicInvocation(mv, "unsignedRShift", 1);
                    ip++;
                }
                case SETATTR -> {
                    generateFieldSet(cw, mv);
                    ip++;
                }
                case GETATTR -> {
                    generateFieldGet(cw, mv);
                    ip++;
                }
                case GETAT -> {
                    generateDynamicInvocation(mv, "getAt", 1);
                    ip++;
                }
                case SETAT -> {
                    generateDynamicInvocation(mv, "setAt", 1);
                    ip++;
                }
                case GETLOCAL -> {
                    generateLocalGet(mv, instructions[ip + 1]);
                    ip += 2;
                }
                case SETLOCAL -> {
                    generateLocalSet(mv, instructions[ip + 1]);
                    ip += 2;
                }
                case TRUTH -> {
                    generateDynamicInvocation(mv, "truth", 0);
                    ip++;
                }
                case NOT -> {
                    generateDynamicInvocation(mv, "truth", 0);
                    generateBoxedBooleanNegation(cw, mv);
                    ip++;
                }
                case AS -> {
                    generateDynamicInvocation(mv, "as", 1);
                    ip++;
                }
                case IF -> {
                    generateDynamicInvocation(mv, "truth", 0);
                    generateIfJump(cw, mv, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                case CALL -> {
                    generateDynamicInvocation(mv, "call", instructions[ip + 1]);
                    ip += 2;
                }
                case CALLAT -> {

                    byte pCount = instructions[ip + 1];
                    String methodName = (String) method.getConstantPool()[fetchInt(instructions, ip + 2)];

                    generateDynamicInvocation(mv, methodName, pCount);

                    ip += 6;
                }
                case GOTO -> {
                    generateGoto(cw, mv, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                case THROW -> {
                    generateThrow(cw, mv);
                    ip++;
                }
                case RETURN -> {
                    generateReturn(mv);
                    ip++;
                }
                case POP -> {
                    generatePop(mv);
                    ip++;
                }
                case DUP -> {
                    generateDup(mv, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                case SWAP -> {
                    generateSwap(mv, fetchInt(instructions, ip + 1), fetchInt(instructions, ip + 5));
                    ip += 9;
                }
                case PUSH -> {
                    generatePush(mv, method.getConstantPool()[fetchInt(instructions, ip + 1)]);
                    ip += 5;
                }
                case EQ -> {
                    generateDynamicInvocation(mv, "equals", 1);
                    ip++;
                }
                case GT -> {
                    generateDynamicInvocation(mv, "compare", 1);
                    generateGreaterThan(cw, mv);
                    ip++;
                }
                case LT -> {
                    generateDynamicInvocation(mv, "compare", 1);
                    generateLessThan(cw, mv);
                    ip++;
                }
                case GE -> {
                    generateDynamicInvocation(mv, "compare", 1);
                    generateGreaterThanOrEqual(cw, mv);
                    ip++;
                }
                case LE -> {
                    generateDynamicInvocation(mv, "compare", 1);
                    generateLessThanOrEqual(cw, mv);
                    ip++;
                }
                case IS -> {
                    generateReferentialEqualityCheck(cw, mv);
                    ip++;
                }
                case INSTANCEOF -> {
                    generateDynamicInvocation(mv, "instanceOf", 1);
                    ip++;
                }
                case ITER -> {
                    generateDynamicInvocation(mv, "instanceOf", 1);
                    ip++;
                }
                case NEXT -> {
                    generateDup(mv, 0);
                    generateIteratorNext(cw, mv, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                case RANGE -> {
                    generatePush(mv, instructions[ip + 1] != 0);
                    generateDynamicInvocation(mv, "range", 2);
                    ip += 2;
                }
                case LIST -> {
                    generateList(mv, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                case MAP -> {
                    generateMap(mv, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                /* TODO - this probably is not needed anymore
                case INIT:
                    ins = stack.peek();
                    stack.push(((Initializable) ins).getInitializer());
                    ip++;
                    break;*/
                case GETMODULE -> {
                    String varName = (String) method.getConstantPool()[fetchInt(instructions, ip + 1)];
                    generateGetModule(cw, mv, varName);
                    ip += 5;
                }
                case SETMODULE -> {
                    String varName = (String) method.getConstantPool()[fetchInt(instructions, ip + 1)];
                    generateSetModule(cw, mv, varName);
                    ip += 5;
                }
                default -> throw new InvalidOpcodeChipmunk(op);
            }

            //mv.visitInsn(Opcodes.ACONST_NULL);
            //mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private int fetchInt(byte[] instructions, int ip) {
        int b1 = instructions[ip] & 0xFF;
        int b2 = instructions[ip + 1] & 0xFF;
        int b3 = instructions[ip + 2] & 0xFF;
        int b4 = instructions[ip + 3] & 0xFF;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    protected void generateDynamicInvocation(MethodVisitor mv, String method, int paramCount) {
        final Type objType = Type.getType(Object.class);

        Type[] pTypes = new Type[paramCount];
        Arrays.fill(pTypes, objType);

        Type callType = Type.getMethodType(objType, pTypes);

        Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC,
                Type.getType(Binder.class).getInternalName(),
                Binder.INDY_BOOTSTRAP_METHOD,
                Binder.bootstrapCallsiteType().toMethodDescriptorString(),
                false);

        mv.visitInvokeDynamicInsn(method, callType.getDescriptor(), bootstrap);

    }

    protected void generateBoxedAnd(ClassWriter cw, MethodVisitor mv){
        // TODO - get two boxed booleans off the stack, calculate &&, and push the boxed result
    }

    protected void generateBoxedOr(ClassWriter cw, MethodVisitor mv){
        // TODO - get two boxed booleans off the stack, calculate ||, and push the boxed result
    }

    protected void generateBoxedBooleanNegation(ClassWriter cw, MethodVisitor mv){
        // TODO - get boxed boolean off the stack, negate, and push the boxed result
    }

    protected void generateGreaterThan(ClassWriter cw, MethodVisitor mv){
        // TODO - get boxed int, push boxed boolean
    }

    protected void generateLessThan(ClassWriter cw, MethodVisitor mv){
        // TODO - get boxed int, push boxed boolean
    }

    protected void generateGreaterThanOrEqual(ClassWriter cw, MethodVisitor mv){
        // TODO - get boxed int, push boxed boolean
    }

    protected void generateLessThanOrEqual(ClassWriter cw, MethodVisitor mv){
        // TODO - get boxed int, push boxed boolean
    }

    protected void generateReferentialEqualityCheck(ClassWriter cw, MethodVisitor mv){
        // TODO - reference equals
    }

    protected void generateFieldSet(ClassWriter cw, MethodVisitor mv){
        // TODO - generate a dynamically invoked field setter
    }

    protected void generateFieldGet(ClassWriter cw, MethodVisitor mv){
        // TODO - generate a dynamically invoked field getter
    }

    protected void generateLocalSet(MethodVisitor mv, byte index){
        mv.visitVarInsn(Opcodes.ASTORE, index);
    }

    protected void generateLocalGet(MethodVisitor mv, byte index){
        mv.visitVarInsn(Opcodes.ALOAD, index);
    }

    protected void generateIfJump(ClassWriter cw, MethodVisitor mv, int jumpTarget){
        // TODO
    }

    protected void generateGoto(ClassWriter cw, MethodVisitor mv, int jumpTarget){
        // TODO
    }

    protected void generateThrow(ClassWriter cw, MethodVisitor mv){
        // TODO
    }

    protected void generateReturn(MethodVisitor mv){
        mv.visitInsn(Opcodes.ARETURN);
    }

    protected void generatePop(MethodVisitor mv){
        mv.visitInsn(Opcodes.POP);
    }

    protected void generatePush(MethodVisitor mv, Object constant){
        if(constant != null){
            if(constant instanceof Boolean){
                mv.visitLdcInsn(((Boolean) constant) ? (byte)1 : (byte)0); // TODO - new boxed boolean
            }else{
                mv.visitLdcInsn(constant);
            }
        }else{
            mv.visitInsn(Opcodes.ACONST_NULL);
        }
    }

    protected void generateDup(MethodVisitor mv, int stackIndex){
        // TODO
    }

    protected void generateSwap(MethodVisitor mv, int index1, int index2){
        // TODO
    }

    protected void generateIteratorNext(ClassWriter cw, MethodVisitor mv, int jumpTarget){
        // TODO
    }

    protected void generateList(MethodVisitor mv, int elementCount){
        generatePush(mv, elementCount);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getType(ArrayList.class).getInternalName(),
                "<init>",
                Type.getMethodType(Type.VOID_TYPE, Type.getType(Integer.class)).getDescriptor(),
                false);

        // TODO - stack format is wrong - elements to add are *above* the array list instance
        for(int i = 0; i < elementCount; i++){
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(ArrayList.class).getInternalName(),
                    "add",
                    Type.getMethodType(Type.getType(Boolean.class), Type.getType(Object.class)).getDescriptor(),
                    true);
            mv.visitInsn(Opcodes.POP);
        }
        mv.visitInsn(Opcodes.POP);
    }

    protected void generateMap(MethodVisitor mv, int elementCount){
        generatePush(mv, elementCount);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getType(HashMap.class).getInternalName(),
                "<init>",
                Type.getMethodType(Type.VOID_TYPE, Type.getType(Integer.class)).getDescriptor(),
                false);

        // TODO - stack format is wrong - elements to add are *above* the hash map instance
        for(int i = 0; i < elementCount; i++){
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(HashMap.class).getInternalName(),
                    "put",
                    Type.getMethodType(Type.getType(Boolean.class), Type.getType(Object.class)).getDescriptor(),
                    true);
            mv.visitInsn(Opcodes.POP);
        }
        mv.visitInsn(Opcodes.POP);
    }

    protected void generateGetModule(ClassWriter cw, MethodVisitor mv, String varName){
        // TODO
    }

    protected void generateSetModule(ClassWriter cw, MethodVisitor mv, String varName){
        // TODO
    }
}

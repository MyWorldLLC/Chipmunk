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
        //generatePush(mv, 1);
        //generatePush(mv, 2);
        //generateDynamicInvocation(mv, "plus", 1);
        //mv.visitInsn(Opcodes.ARETURN);
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

            switch (op) {
                case ADD -> {
                    generateDynamicInvocation(mv, "plus", 2);
                    ip++;
                }
                case SUB -> {
                    generateDynamicInvocation(mv, "minus", 2);
                    ip++;
                }
                case MUL -> {
                    generateDynamicInvocation(mv, "mul", 2);
                    ip++;
                }
                case DIV -> {
                    generateDynamicInvocation(mv, "div", 2);
                    ip++;
                }
                case FDIV -> {
                    generateDynamicInvocation(mv, "fdiv", 2);
                    ip++;
                }
                case MOD -> {
                    generateDynamicInvocation(mv, "mod", 2);
                    ip++;
                }
                case POW -> {
                    generateDynamicInvocation(mv, "pow", 2);
                    ip++;
                }
                case INC -> {
                    generateDynamicInvocation(mv, "inc", 1);
                    ip++;
                }
                case DEC -> {
                    generateDynamicInvocation(mv, "dec", 1);
                    ip++;
                }
                case POS -> {
                    generateDynamicInvocation(mv, "pos", 1);
                    ip++;
                }
                case NEG -> {
                    generateDynamicInvocation(mv, "neg", 1);
                    ip++;
                }
                case BXOR -> {
                    generateDynamicInvocation(mv, "binaryXor", 2);
                    ip++;
                }
                case BAND -> {
                    generateDynamicInvocation(mv, "binaryAnd", 2);
                    ip++;
                }
                case BOR -> {
                    generateDynamicInvocation(mv, "binaryOr", 2);
                    ip++;
                }
                case BNEG -> {
                    generateDynamicInvocation(mv, "binaryNeg", 2);
                    ip++;
                }
                case LSHIFT -> {
                    generateDynamicInvocation(mv, "lShift", 2);
                    ip++;
                }
                case RSHIFT -> {
                    generateDynamicInvocation(mv, "rShift", 2);
                    ip++;
                }
                case URSHIFT -> {
                    generateDynamicInvocation(mv, "unsignedRShift", 2);
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
                    generateDynamicInvocation(mv, "getAt", 2);
                    ip++;
                }
                case SETAT -> {
                    generateDynamicInvocation(mv, "setAt", 3);
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
                    generateDynamicInvocation(mv, "truth", 1);
                    ip++;
                }
                case NOT -> {
                    generateDynamicInvocation(mv, "truth", 1);
                    generateBoxedBooleanNegation(mv);
                    ip++;
                }
                case AS -> {
                    generateDynamicInvocation(mv, "as", 2);
                    ip++;
                }
                case IF -> {
                    generateDynamicInvocation(mv, "truth", 1);
                    generateIfJump(cw, mv, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                case CALL -> {
                    generateDynamicInvocation(mv, "call", (byte) (instructions[ip + 1]));
                    ip += 2;
                }
                case CALLAT -> {

                    byte pCount = (byte) (instructions[ip + 1] + 1);
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
                    generateDup(mv);
                    ip++;
                }
                case PUSH -> {
                    generatePush(mv, method.getConstantPool()[fetchInt(instructions, ip + 1)]);
                    ip += 5;
                }
                case EQ -> {
                    generateDynamicInvocation(mv, "equals", 2);
                    ip++;
                }
                case GT -> {
                    generateDynamicInvocation(mv, "compare", 2);
                    generateGreaterThan(mv);
                    ip++;
                }
                case LT -> {
                    generateDynamicInvocation(mv, "compare", 2);
                    generateLessThan(cw, mv);
                    ip++;
                }
                case GE -> {
                    generateDynamicInvocation(mv, "compare", 2);
                    generateGreaterThanOrEqual(cw, mv);
                    ip++;
                }
                case LE -> {
                    generateDynamicInvocation(mv, "compare", 2);
                    generateLessThanOrEqual(cw, mv);
                    ip++;
                }
                case IS -> {
                    generateReferentialEqualityCheck(mv);
                    ip++;
                }
                case INSTANCEOF -> {
                    generateDynamicInvocation(mv, "instanceOf", 2);
                    ip++;
                }
                case ITER -> {
                    generateDynamicInvocation(mv, "instanceOf", 2);
                    ip++;
                }
                case NEXT -> {
                    generateDup(mv);
                    generateIteratorNext(cw, mv, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                case RANGE -> {
                    generatePush(mv, instructions[ip + 1] != 0);
                    generateDynamicInvocation(mv, "range", 3);
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
                /*//TODO - this probably is not needed anymore
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

        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();
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

    protected void generateBoxedAnd(MethodVisitor mv){

        // Get two boxed booleans off the stack, calculate &&, and push the boxed result
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(Boolean.class).getInternalName(),
                "booleanValue",
                Type.getMethodType(Type.BOOLEAN_TYPE).getDescriptor(),
                false);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(Boolean.class).getInternalName(),
                "booleanValue",
                Type.getMethodType(Type.BOOLEAN_TYPE).getDescriptor(),
                false);

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getType(Boolean.class).getInternalName(),
                "logicalAnd",
                Type.getMethodType(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE).getDescriptor(),
                false);


        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getType(Boolean.class).getInternalName(),
                "valueOf",
                Type.getMethodType(Type.getType(Boolean.class), Type.BOOLEAN_TYPE).getDescriptor(),
                false);
    }

    protected void generateBoxedBooleanNegation(MethodVisitor mv){
        // Get boxed boolean off the stack, negate, and push the boxed result
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Boolean.class).getInternalName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(Boolean.class).getInternalName(),
                "booleanValue",
                Type.getMethodType(Type.BOOLEAN_TYPE).getDescriptor(),
                false);

        // TODO - generate negation

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getType(Boolean.class).getInternalName(),
                "valueOf",
                Type.getMethodType(Type.getType(Boolean.class), Type.BOOLEAN_TYPE).getDescriptor(),
                false);
    }

    protected void generateGreaterThan(MethodVisitor mv){
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

    protected void generateReferentialEqualityCheck(MethodVisitor mv){
        // Reference equals
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
                mv.visitLdcInsn(((Boolean) constant) ? (byte)1 : (byte)0);
            }else{
                mv.visitLdcInsn(constant);
            }
            generateBoxing(mv, constant);
        }else{
            mv.visitInsn(Opcodes.ACONST_NULL);
        }
    }

    protected void generateDup(MethodVisitor mv){
        mv.visitInsn(Opcodes.DUP);
    }

    protected void generateIteratorNext(ClassWriter cw, MethodVisitor mv, int jumpTarget){
        // TODO
    }

    protected void generateList(MethodVisitor mv, int elementCount){
        mv.visitTypeInsn(Opcodes.NEW, Type.getType(ArrayList.class).getInternalName());
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(elementCount);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getType(ArrayList.class).getInternalName(),
                "<init>",
                Type.getMethodType(Type.VOID_TYPE, Type.INT_TYPE).getDescriptor(),
                false);
    }

    protected void generateMap(MethodVisitor mv, int elementCount){
        mv.visitTypeInsn(Opcodes.NEW, Type.getType(HashMap.class).getInternalName());
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(elementCount);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getType(HashMap.class).getInternalName(),
                "<init>",
                Type.getMethodType(Type.VOID_TYPE, Type.INT_TYPE).getDescriptor(),
                false);
    }

    protected void generateGetModule(ClassWriter cw, MethodVisitor mv, String varName){
        // TODO
    }

    protected void generateSetModule(ClassWriter cw, MethodVisitor mv, String varName){
        // TODO
    }

    protected void generateBoxing(MethodVisitor mv, Object o){
        if(o instanceof Byte){
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getType(Byte.class).getInternalName(),
                    "valueOf",
                    Type.getMethodType(Type.getType(Byte.class), Type.BYTE_TYPE).getDescriptor(),
                    false);
        }else if(o instanceof Short){
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getType(Short.class).getInternalName(),
                    "valueOf",
                    Type.getMethodType(Type.getType(Short.class), Type.SHORT_TYPE).getDescriptor(),
                    false);
        }else if(o instanceof Integer){
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getType(Integer.class).getInternalName(),
                    "valueOf",
                    Type.getMethodType(Type.getType(Integer.class), Type.INT_TYPE).getDescriptor(),
                    false);
        }else if(o instanceof Long){
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getType(Long.class).getInternalName(),
                    "valueOf",
                    Type.getMethodType(Type.getType(Long.class), Type.LONG_TYPE).getDescriptor(),
                    false);
        }else if(o instanceof Boolean){
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getType(Boolean.class).getInternalName(),
                    "valueOf",
                    Type.getMethodType(Type.getType(Boolean.class), Type.BOOLEAN_TYPE).getDescriptor(),
                    false);
        }else if(o instanceof Float){
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getType(Float.class).getInternalName(),
                    "valueOf",
                    Type.getMethodType(Type.getType(Float.class), Type.FLOAT_TYPE).getDescriptor(),
                    false);
        }else if(o instanceof Double){
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getType(Double.class).getInternalName(),
                    "valueOf",
                    Type.getMethodType(Type.getType(Double.class), Type.DOUBLE_TYPE).getDescriptor(),
                    false);
        }
    }

    protected void generateUnboxing(MethodVisitor mv, Class<?> cls) {
        if (Byte.class.equals(cls)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Byte.class).getInternalName(),
                    "byteValue",
                    Type.getMethodType(Type.BYTE_TYPE).getDescriptor(),
                    false);
        } else if (Short.class.equals(cls)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Short.class).getInternalName(),
                    "shortValue",
                    Type.getMethodType(Type.SHORT_TYPE).getDescriptor(),
                    false);
        } else if (Integer.class.equals(cls)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Integer.class).getInternalName(),
                    "intValue",
                    Type.getMethodType(Type.INT_TYPE).getDescriptor(),
                    false);
        } else if (Long.class.equals(cls)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Long.class).getInternalName(),
                    "longValue",
                    Type.getMethodType(Type.LONG_TYPE).getDescriptor(),
                    false);
        } else if (Boolean.class.equals(cls)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Boolean.class).getInternalName(),
                    "booleanValue",
                    Type.getMethodType(Type.BOOLEAN_TYPE).getDescriptor(),
                    false);
        } else if (Float.class.equals(cls)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Float.class).getInternalName(),
                    "floatValue",
                    Type.getMethodType(Type.FLOAT_TYPE).getDescriptor(),
                    false);
        } else if (Double.class.equals(cls)) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Double.class).getInternalName(),
                    "doubleValue",
                    Type.getMethodType(Type.DOUBLE_TYPE).getDescriptor(),
                    false);
        }
    }
}
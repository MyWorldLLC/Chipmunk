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

import chipmunk.binary.DebugEntry;
import chipmunk.InvalidOpcodeChipmunk;
import chipmunk.binary.*;
import chipmunk.invoke.Binder;
import chipmunk.runtime.ChipmunkModule;
import org.objectweb.asm.*;

import java.util.*;

import static chipmunk.Opcodes.*;

public class JvmCompiler {

    private final ChipmunkClassLoader loader;
    protected final CompiledMethods methods;

    public JvmCompiler(){
        loader = new ChipmunkClassLoader();
        methods = new CompiledMethods();
    }

    public ChipmunkModule compile(BinaryModule module){

        Type objType = Type.getType(Object.class);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, module.getName(), null, objType.getInternalName(),
                new String[]{
                        Type.getType(ChipmunkModule.class).getInternalName()
                        //Type.getType(ModuleMember.class).getInternalName()
        });
        cw.visitSource(module.getName() + ".chp", null);

        // Implement ChipmunkModule interface

        // ChipmunkModule.getName()
        cw.visitField(Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, "$name",
                Type.getType(String.class).getDescriptor(), null, null);

        MethodVisitor getName = cw.visitMethod(Opcodes.ACC_PUBLIC, "getName", Type.getMethodType(Type.getType(String.class)).getDescriptor(), null, null);
        getName.visitCode();
        getName.visitVarInsn(Opcodes.ALOAD, 0);
        getName.visitFieldInsn(Opcodes.GETFIELD, module.getName().replace('.','/'), "$name", Type.getType(String.class).getDescriptor());
        getName.visitInsn(Opcodes.ARETURN);
        getName.visitMaxs(0, 0);
        getName.visitEnd();

        // ModuleMember.getModule()
       /* MethodVisitor getModule = cw.visitMethod(Opcodes.ACC_PUBLIC, "getModule", Type.getMethodType(Type.getType(ChipmunkModule.class)).getDescriptor(), null, null);
        getModule.visitCode();
        getModule.visitVarInsn(Opcodes.ALOAD, 0);
        getModule.visitInsn(Opcodes.ARETURN);
        getModule.visitMaxs(0, 0);
        getModule.visitEnd();*/

        // ChipmunkModule.getDepedencies()
        /*cw.visitField(Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, "$depedencies",
                Type.getType(String[].class).getDescriptor(), null, null);

        MethodVisitor getDependencies = cw.visitMethod(Opcodes.ACC_PUBLIC, "getDependencies", Type.getMethodType(Type.getType(String[].class)).getDescriptor(), null, null);
        getDependencies.visitCode();
        getDependencies.visitVarInsn(Opcodes.ALOAD, 0);
        getDependencies.visitFieldInsn(Opcodes.GETFIELD, module.getName().replace('.','/'), "$dependencies", Type.getType(String[].class).getDescriptor());
        getDependencies.visitInsn(Opcodes.DUP);
        getDependencies.visitInsn(Opcodes.ARRAYLENGTH);
        getDependencies.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getType(Arrays.class).getInternalName(), "copyOf", Type.getMethodType(Type.getType(String[].class), Type.INT_TYPE).getDescriptor(), false);
        getDependencies.visitInsn(Opcodes.ARETURN);
        getDependencies.visitMaxs(0, 0);
        getDependencies.visitEnd();*/

        // Module constructor/initializer
        MethodVisitor moduleInit = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
        moduleInit.visitCode();
        moduleInit.visitVarInsn(Opcodes.ALOAD, 0);
        moduleInit.visitMethodInsn(Opcodes.INVOKESPECIAL, objType.getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);

        // Init module name
        moduleInit.visitVarInsn(Opcodes.ALOAD, 0);
        moduleInit.visitLdcInsn(module.getName());
        moduleInit.visitFieldInsn(Opcodes.PUTFIELD, module.getName().replace('.', '/'), "$name", Type.getType(String.class).getDescriptor());

        // Init module dependencies
        //moduleInit.visitVarInsn(Opcodes.ALOAD, 0);


        for(BinaryNamespace.Entry entry : module.getNamespace()){
            int flags = Opcodes.ACC_PUBLIC;
            if(BinaryConstants.isFlagSet(entry.getFlags(), BinaryConstants.FINAL_FLAG)){
                flags |= Opcodes.ACC_FINAL;
            }

            if(entry.getType() == FieldType.METHOD){
                visitMethod(cw, flags, entry.getName(), entry.getBinaryMethod());
            }else if(entry.getType() == FieldType.CLASS){
                // TODO - generate class, add field to module, and add class initialization call to constructor
            }else{
                visitVar(cw, flags, entry.getName());
            }
        }

        moduleInit.visitInsn(Opcodes.RETURN);
        moduleInit.visitMaxs(0, 0);
        moduleInit.visitEnd();

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        return (ChipmunkModule) instantiate(loadClass(module.getName(), bytes));
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

        MethodVisitor mv = cw.visitMethod(flags, name, methodType.getDescriptor(), null, null);
        mv.visitCode();

        Map<Integer, Label> labelMappings = new HashMap<>();
        DebugEntry[] debugTable = method.getDebugTable();
        int debugIndex = 0;

        byte[] instructions = method.getCode();
        for(int ip = 0; ip < instructions.length;) {

            // Visit labels before generating bytecode
            Label label = markLabel(ip, labelMappings);
            mv.visitLabel(label);

            if(debugTable != null && debugIndex < debugTable.length){
                DebugEntry entry = debugTable[debugIndex];
                // This isn't technically correct but it prevents crashes if the debug table is malformed
                if(entry.endIndex <= ip && debugIndex + 1 < debugTable.length){
                    debugIndex++;
                    entry = debugTable[debugIndex];
                }
                mv.visitLineNumber(entry.lineNumber, label);
            }

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
                    String attr = (String) method.getConstantPool()[fetchInt(instructions, ip + 1)];
                    generateFieldSet(mv, attr);
                    ip += 5;
                }
                case GETATTR -> {
                    String attr = (String) method.getConstantPool()[fetchInt(instructions, ip + 1)];
                    generateFieldGet(mv, attr);
                    ip += 5;
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
                    generateIfJump(mv, labelMappings, fetchInt(instructions, ip + 1));
                    ip += 5;
                }
                case CALL -> {
                    generateDynamicInvocation(mv, "call", (byte) (instructions[ip + 1] + 1));
                    ip += 2;
                }
                case CALLAT -> {

                    byte pCount = (byte) (instructions[ip + 1] + 1);
                    String methodName = (String) method.getConstantPool()[fetchInt(instructions, ip + 2)];

                    generateDynamicInvocation(mv, methodName, pCount);

                    ip += 6;
                }
                case GOTO -> {
                    generateGoto(mv, fetchInt(instructions, ip + 1), labelMappings);
                    ip += 5;
                }
                case THROW -> {
                    generateThrow(mv);
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
                    generateLessThan(mv);
                    ip++;
                }
                case GE -> {
                    generateDynamicInvocation(mv, "compare", 2);
                    generateGreaterThanOrEqual(mv);
                    ip++;
                }
                case LE -> {
                    generateDynamicInvocation(mv, "compare", 2);
                    generateLessThanOrEqual(mv);
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
                    generateDynamicInvocation(mv, "iterator", 1);
                    ip++;
                }
                case NEXT -> {
                    generateDup(mv);
                    generateIteratorNext(mv, fetchInt(instructions, ip + 1), labelMappings);
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
                case GETMODULE -> {
                    String varName = (String) method.getConstantPool()[fetchInt(instructions, ip + 1)];
                    generateGetModule(mv, varName);
                    ip += 5;
                }
                case SETMODULE -> {
                    String varName = (String) method.getConstantPool()[fetchInt(instructions, ip + 1)];
                    generateSetModule(mv, varName);
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

    protected void generateDynamicFieldAccess(MethodVisitor mv, String field, boolean set) {
        final Type objType = Type.getType(Object.class);

        Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC,
                Type.getType(Binder.class).getInternalName(),
                set ? Binder.INDY_BOOTSTRAP_SET : Binder.INDY_BOOTSTRAP_GET,
                Binder.bootstrapFieldOpType().toMethodDescriptorString(),
                false);

        mv.visitInvokeDynamicInsn(field, Type.getMethodType(objType, objType).getDescriptor(), bootstrap);

    }

    protected void generateBoxedBooleanNegation(MethodVisitor mv){

        generateUnboxing(mv, Boolean.class);

        mv.visitLdcInsn(1);
        mv.visitInsn(Opcodes.IXOR);

        generateBoxing(mv, true); // Just need a boolean value here - false is also fine

    }

    protected void generateGreaterThan(MethodVisitor mv){
        generateUnboxing(mv, Integer.class);
        // Need to use the opposite operation in JVM bytecode because Java has the
        // stack backwards from how we have it
        generateTest(mv, Opcodes.IFLE);
    }

    protected void generateLessThan(MethodVisitor mv){
        generateUnboxing(mv, Integer.class);
        // Need to use the opposite operation in JVM bytecode because Java has the
        // stack backwards from how we have it
        generateTest(mv, Opcodes.IFGE);
    }

    protected void generateGreaterThanOrEqual(MethodVisitor mv){
        generateUnboxing(mv, Integer.class);
        // Need to use the opposite operation in JVM bytecode because Java has the
        // stack backwards from how we have it
        generateTest(mv, Opcodes.IFLT);
    }

    protected void generateLessThanOrEqual(MethodVisitor mv){
        generateUnboxing(mv, Integer.class);
        // Need to use the opposite operation in JVM bytecode because Java has the
        // stack backwards from how we have it
        generateTest(mv, Opcodes.IFGE);
    }

    protected void generateReferentialEqualityCheck(MethodVisitor mv){
        // Reference equals
        generateTest(mv, Opcodes.IF_ACMPNE);
    }

    protected void generateTest(MethodVisitor mv, int ifOpCode){
        Label endLabel = new Label();
        Label falseLabel = new Label();

        mv.visitJumpInsn(ifOpCode, falseLabel);

        // The value was true
        mv.visitLdcInsn(1);

        mv.visitJumpInsn(Opcodes.GOTO, endLabel);

        // The value was false
        mv.visitLabel(falseLabel);
        mv.visitLdcInsn(0);

        mv.visitLabel(endLabel);

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getType(Boolean.class).getInternalName(),
                "valueOf",
                Type.getMethodType(Type.getType(Boolean.class), Type.BOOLEAN_TYPE).getDescriptor(),
                false);
    }

    protected void generateFieldSet(MethodVisitor mv, String attr){
        generateDynamicFieldAccess(mv, attr, true);
    }

    protected void generateFieldGet(MethodVisitor mv, String attr){
        generateDynamicFieldAccess(mv, attr, false);
    }

    protected void generateLocalSet(MethodVisitor mv, byte index){
        mv.visitVarInsn(Opcodes.ASTORE, index);
    }

    protected void generateLocalGet(MethodVisitor mv, byte index){
        mv.visitVarInsn(Opcodes.ALOAD, index);
    }

    protected void generateIfJump(MethodVisitor mv, Map<Integer, Label> labels, int jumpTarget){
        Label target = markLabel(jumpTarget, labels);

        generateUnboxing(mv, Boolean.class);

        mv.visitJumpInsn(Opcodes.IFEQ, target);
    }

    protected void generateGoto(MethodVisitor mv, int jumpTarget, Map<Integer, Label> labelMappings){
        mv.visitJumpInsn(Opcodes.GOTO, markLabel(jumpTarget, labelMappings));
    }

    protected void generateThrow(MethodVisitor mv){
        // TODO - test if the object on the stack is a Throwable or not.
        // If not, wrap in an AngryChipmunk & throw, else throw unmodified.
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
                mv.visitLdcInsn(((Boolean) constant) ? 1 : 0);
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

    protected void generateIteratorNext(MethodVisitor mv, int jumpTarget, Map<Integer, Label> labelMappings){

        generateDynamicInvocation(mv, "hasNext", 1);

        generateUnboxing(mv, Boolean.class);

        // Jump if there's no next
        Label loopEnd = markLabel(jumpTarget, labelMappings);
        mv.visitJumpInsn(Opcodes.IFEQ, loopEnd);

        mv.visitInsn(Opcodes.DUP);
        // There is a next element, so fetch it
        generateDynamicInvocation(mv, "next", 1);
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

    protected void generateGetModule(MethodVisitor mv, String varName){
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        /*mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getType(ModuleMember.class).getInternalName(),
                "getModule",
                Type.getMethodType(Type.getType(ChipmunkModule.class)).getDescriptor(),
                true);*/

        generateDynamicFieldAccess(mv, varName, false);
    }

    protected void generateSetModule(MethodVisitor mv, String varName){
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        /*mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getType(ModuleMember.class).getInternalName(),
                "getModule",
                Type.getMethodType(Type.getType(ChipmunkModule.class)).getDescriptor(),
                true);*/
        // Swap the module & the value
        mv.visitInsn(Opcodes.SWAP);

        generateDynamicFieldAccess(mv, varName, true);
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
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Byte.class).getInternalName());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Byte.class).getInternalName(),
                    "byteValue",
                    Type.getMethodType(Type.BYTE_TYPE).getDescriptor(),
                    false);
        } else if (Short.class.equals(cls)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Short.class).getInternalName());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Short.class).getInternalName(),
                    "shortValue",
                    Type.getMethodType(Type.SHORT_TYPE).getDescriptor(),
                    false);
        } else if (Integer.class.equals(cls)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Integer.class).getInternalName());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Integer.class).getInternalName(),
                    "intValue",
                    Type.getMethodType(Type.INT_TYPE).getDescriptor(),
                    false);
        } else if (Long.class.equals(cls)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Long.class).getInternalName());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Long.class).getInternalName(),
                    "longValue",
                    Type.getMethodType(Type.LONG_TYPE).getDescriptor(),
                    false);
        } else if (Boolean.class.equals(cls)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Boolean.class).getInternalName());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Boolean.class).getInternalName(),
                    "booleanValue",
                    Type.getMethodType(Type.BOOLEAN_TYPE).getDescriptor(),
                    false);
        } else if (Float.class.equals(cls)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Float.class).getInternalName());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Float.class).getInternalName(),
                    "floatValue",
                    Type.getMethodType(Type.FLOAT_TYPE).getDescriptor(),
                    false);
        } else if (Double.class.equals(cls)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Double.class).getInternalName());
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getType(Double.class).getInternalName(),
                    "doubleValue",
                    Type.getMethodType(Type.DOUBLE_TYPE).getDescriptor(),
                    false);
        }
    }

    protected Label markLabel(int chpTarget, Map<Integer, Label> unresolved){
        if(!unresolved.containsKey(chpTarget)){
            unresolved.put(chpTarget, new Label());
        }
        return unresolved.get(chpTarget);
    }
}

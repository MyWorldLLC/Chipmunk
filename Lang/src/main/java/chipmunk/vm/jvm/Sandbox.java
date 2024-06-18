/*
 * Copyright (C) 2023 MyWorld, LLC
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

package chipmunk.vm.jvm;

import chipmunk.vm.ChipmunkVM;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Sandbox extends MethodVisitor {

    public record TryCatch(Label start, Label end, Label handler, Label chainHandler){

        public boolean sameBlock(Label s, Label e){
            return start == s && end == e;
        }

    }

    protected final SandboxContext sandbox;
    protected int lineNumber;
    protected final List<Label> visited;
    protected final List<TryCatch> guardedBlocks;
    protected int trap;
    protected int methodId;

    public Sandbox(MethodVisitor delegate, SandboxContext sandbox){
        super(Opcodes.ASM9, delegate);
        this.sandbox = sandbox;

        visited = new ArrayList<>();
        guardedBlocks = new ArrayList<>();
    }

    @Override
    public void visitLineNumber(int lineNumber, Label start){
        this.lineNumber = lineNumber;
        super.visitLineNumber(lineNumber, start);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label){
        if(labelVisited(label) && sandbox.getTrapConfig().isEnabled(TrapFlag.BACK_JUMP)){
            // Label has already been visited, so this is a backjump
            generateBackJumpTrap();
        }
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(final Label label){

        super.visitLabel(label);

        // If this is the end of a try-catch block,
        // insert the uncatchable handler
        var handledBlock = handledBlock(label);
        handledBlock.ifPresent(this::insertUncatchableHandler);

        // Mark labels as we visit them so we can identify backjumps
        if(!labelVisited(label)){
            visited.add(label);
        }
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, Label handler, final String type){

        if(!isGuardedBlock(start, end)){

            // Sometimes end == handler. In that case, we need to split them - a new handler label is created
            // and passed to the original block's visit call below, and the new label is tracked with the guarded
            // block and visited at the end of generating the uncatchable handler.
            Label chainHandler = null;
            if(end == handler){
                chainHandler = new Label();
                handler = chainHandler;
            }


            for(var uncatchable : sandbox.getUncatchable()){
                var uncatchableHandler = new Label();

                super.visitTryCatchBlock(start, end, uncatchableHandler, Type.getInternalName(uncatchable));
                guardedBlocks.add(new TryCatch(start, end, uncatchableHandler, chainHandler));
            }
        }
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type){
        if(opcode == Opcodes.NEW && sandbox.getTrapConfig().isEnabled(TrapFlag.OBJECT_ALLOC)){
            generateObjectAllocTrap(TrapSite.Position.PRE, type);
        }else if((opcode == Opcodes.ANEWARRAY)
                && sandbox.getTrapConfig().isEnabled(TrapFlag.ARRAY_ALLOC)){
            generateArrayAllocTrap(TrapSite.Position.PRE, type, 1);
        }

        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand){
        if(opcode == Opcodes.NEWARRAY && sandbox.getTrapConfig().isEnabled(TrapFlag.ARRAY_ALLOC)){
            generateArrayAllocTrap(TrapSite.Position.PRE, arrayOperandToClassName(operand), 1);
        }
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions){
        if(sandbox.getTrapConfig().isEnabled(TrapFlag.ARRAY_ALLOC)){
            generateArrayAllocTrap(TrapSite.Position.PRE, descriptor, numDimensions);
        }
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface){
        if(sandbox.getTrapConfig().isEnabled(TrapFlag.METHOD_CALL, TrapFlag.PRE_METHOD_CALL)){
            generateMethodTrap(TrapSite.Position.PRE, owner, name, descriptor);
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if(opcode == Opcodes.INVOKESPECIAL && name.equals("<init>") && sandbox.getTrapConfig().isEnabled(TrapFlag.OBJECT_INIT)){
            generateInitTrap();
        }
        if(sandbox.getTrapConfig().isEnabled(TrapFlag.METHOD_CALL, TrapFlag.POST_METHOD_CALL)){
            generateMethodTrap(TrapSite.Position.POST, owner, name, descriptor);
        }
    }

    protected boolean labelVisited(Label l){
        return visited.stream().anyMatch(label -> label == l);
    }

    protected boolean isGuardedBlock(Label start, Label end){
        return guardedBlocks.stream().anyMatch(t -> t.sameBlock(start, end));
    }

    protected Optional<TryCatch> handledBlock(Label end){
        return guardedBlocks.stream().filter(tc -> tc.end() == end).findFirst();
    }

    protected void unmarkGuardedBlock(Label start, Label end){
        guardedBlocks.removeIf(tc -> tc.sameBlock(start, end));
    }

    protected void insertUncatchableHandler(TryCatch tc){

        var uncaughtEntries = guardedBlocks.stream()
                .filter(t -> t.sameBlock(tc.start(), tc.end()))
                .toList();

        // First generate the re-throwers
        for(var uncaughtEntry : uncaughtEntries){
            super.visitLabel(uncaughtEntry.handler());
            visitInsn(Opcodes.ATHROW);
        }

        // After the re-throw sections, mark the chained handlers for the block
        for(var uncaughtEntry : uncaughtEntries){
            if(uncaughtEntry.chainHandler() != null){
                super.visitLabel(uncaughtEntry.chainHandler());
            }
        }

        unmarkGuardedBlock(tc.start(), tc.end());
    }

    protected void generateBackJumpTrap(){
        super.visitLdcInsn(makeTrapSite(TrapSite.Position.PRE));
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(ChipmunkVM.class),
                "trapBackJump",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(TrapSite.class)),
                false);
    }

    protected void generateInitTrap(){
        super.visitInsn(Opcodes.DUP);
        super.visitLdcInsn(makeTrapSite(TrapSite.Position.POST));
        super.visitInsn(Opcodes.SWAP);
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(ChipmunkVM.class),
                "trapObjectInit",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(TrapSite.class), Type.getType(Object.class)),
                false);
    }

    protected void generateMethodTrap(TrapSite.Position pos, String targetClass, String targetMethodName, String targetDescriptor){
        super.visitLdcInsn(makeTrapSite(pos));
        super.visitLdcInsn(makeMethodId(targetClass, targetMethodName, targetDescriptor));
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(ChipmunkVM.class),
                "trapMethodCall",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(TrapSite.class), Type.getType(MethodIdentifier.class)),
                false);
    }

    protected void generateObjectAllocTrap(TrapSite.Position pos, String targetClass){
        super.visitLdcInsn(makeTrapSite(pos));
        super.visitLdcInsn(makeClassConst(targetClass));
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(ChipmunkVM.class),
                "trapObjectAlloc",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(TrapSite.class), Type.getType(Class.class)),
                false);
    }

    protected void generateArrayAllocTrap(TrapSite.Position pos, String arrayClassDescriptor, int dimensions){
        // Only pre-allocation trapping is supported, so top of stack will always be an int defining the
        // array length. Dup/swap it down the parameter chain until it's in the trailing argument position for
        // the trap call
        super.visitInsn(Opcodes.DUP);
        super.visitLdcInsn(makeTrapSite(pos));
        super.visitInsn(Opcodes.SWAP);
        super.visitLdcInsn(makeClassConst(arrayClassDescriptor));
        super.visitInsn(Opcodes.SWAP);
        super.visitLdcInsn(dimensions);
        super.visitInsn(Opcodes.SWAP);
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(ChipmunkVM.class),
                "trapArrayAlloc",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(TrapSite.class), Type.getType(Class.class), Type.INT_TYPE, Type.INT_TYPE),
                false);
    }

    protected ConstantDynamic makeTrapSite(TrapSite.Position pos){

        var paramNames = Arrays.stream(Type.getArgumentTypes(sandbox.getMethodDescriptor()))
                .map(Type::getClassName)
                .toArray(String[]::new);

        return new ConstantDynamic("trap$" + (trap++),
                Type.getDescriptor(TrapSite.class),
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        Type.getInternalName(Sandbox.class),
                        "bootstrapTrapsite",
                        Type.getMethodDescriptor(
                                Type.getType(TrapSite.class),
                                Type.getType(TrapSite.Position.class),
                                Type.getType(String.class),
                                Type.getType(String.class),
                                Type.getType(String.class),
                                Type.getType(String[].class),
                                Type.INT_TYPE
                        ),
                        false
                ),
                pos,
                sandbox.getClassName(),
                sandbox.getMethodName(),
                Type.getReturnType(sandbox.getMethodDescriptor()).getClassName(),
                paramNames,
                lineNumber);
    }

    protected ConstantDynamic makeMethodId(String targetClass, String targetMethodName, String targetDescriptor){
        var paramNames = Arrays.stream(Type.getArgumentTypes(targetDescriptor))
                .map(Type::getClassName)
                .toArray(String[]::new);

        return new ConstantDynamic("method$" + (methodId++),
                Type.getDescriptor(TrapSite.class),
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        Type.getInternalName(Sandbox.class),
                        "bootstrapMethodId",
                        Type.getMethodDescriptor(
                                Type.getType(MethodIdentifier.class),
                                Type.getType(String.class),
                                Type.getType(String.class),
                                Type.getType(String.class),
                                Type.getType(String[].class)
                        ),
                        false
                ),
                targetClass,
                targetMethodName,
                Type.getReturnType(targetDescriptor).getClassName(),
                paramNames,
                lineNumber);
    }

    protected ConstantDynamic makeClassConst(String targetClass) {
        return new ConstantDynamic("cls$" + targetClass,
                Type.getDescriptor(Class.class),
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        Type.getInternalName(Sandbox.class),
                        "classForName",
                        Type.getMethodDescriptor(
                                Type.getType(Class.class),
                                Type.getType(String.class)
                        ),
                        false
                ),
                targetClass);
    }

    protected static Class<?>[] signature(String returnType, String[] params) throws ClassNotFoundException {
        var signature = new Class<?>[params.length + 1];
        signature[0] = classForName(returnType);

        for(int i = 1; i < params.length; i++){
            signature[i] = classForName(params[i - 1]);
        }
        return signature;
    }

    protected static TrapSite bootstrapTrapsite(TrapSite.Position pos, String className, String method, String returnType, String[] params, int line) throws ClassNotFoundException {
        return new TrapSite(pos, new MethodIdentifier(classForName(className), method, signature(returnType, params)), line);
    }

    protected static MethodIdentifier bootstrapMethodId(String className, String methodName, String returnType, String[] params) throws ClassNotFoundException {
        return new MethodIdentifier(classForName(className), methodName, signature(returnType, params));
    }

    protected static Class<?> classForName(String name) throws ClassNotFoundException {
        if(name.contains("[]")){
            // This is an array type
            return arrayClassForName(name);
        }else{
            // This is a plain type
            return plainClassForName(name);
        }
    }

    protected static Class<?> plainClassForName(String name) throws ClassNotFoundException {
        return switch (name){
            case "void" -> void.class;
            case "boolean" -> boolean.class;
            case "byte" -> byte.class;
            case "char" -> char.class;
            case "short" -> short.class;
            case "int" -> int.class;
            case "long" -> long.class;
            case "float" -> float.class;
            case "double" -> double.class;
            default -> Class.forName(name);
        };
    }

    protected static Class<?> arrayClassForName(String name) throws ClassNotFoundException {

        var jvmName = switch(name.replace("[]", "")){
            case "boolean" -> "Z";
            case "byte" -> "B";
            case "char" -> "C";
            case "short" -> "S";
            case "int" -> "I";
            case "long" -> "J";
            case "float" -> "F";
            case "double" -> "D";
            default -> "L" + name + ";";
        };

        var nestingPrefix = Pattern.compile("\\[]")
                .matcher(name)
                .results()
                .map(s -> "[")
                .collect(Collectors.joining());

        return Class.forName(nestingPrefix + jvmName);
    }

    protected String arrayOperandToClassName(int operand){
        return switch (operand){
            case Opcodes.T_BYTE -> "[B";
            case Opcodes.T_BOOLEAN -> "[Z";
            case Opcodes.T_CHAR -> "[C";
            case Opcodes.T_SHORT -> "[S";
            case Opcodes.T_INT -> "[I";
            case Opcodes.T_LONG -> "[J";
            case Opcodes.T_FLOAT -> "[F";
            case Opcodes.T_DOUBLE -> "[D";
            default -> throw new IllegalArgumentException("Unsupported array operand type 0x%X".formatted(operand));
        };
    }

}

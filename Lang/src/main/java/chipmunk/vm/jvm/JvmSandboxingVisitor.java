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

import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JvmSandboxingVisitor extends MethodVisitor {

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

    public JvmSandboxingVisitor(MethodVisitor delegate, SandboxContext sandbox){
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
            // TODO - insert trap call
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
            var uncatchableHandler = new Label();

            // Sometimes end == handler. In that case, we need to split them - a new handler label is created
            // and passed to the original block's visit call below, and the new label is tracked with the guarded
            // block and visited at the end of generating the uncatchable handler.
            Label chainHandler = null;
            if(end == handler){
                chainHandler = new Label();
                handler = chainHandler;
            }

            super.visitTryCatchBlock(start, end, uncatchableHandler, Type.getInternalName(Uncatchable.class));
            guardedBlocks.add(new TryCatch(start, end, uncatchableHandler, chainHandler));
        }

        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type){
        if(opcode == Opcodes.NEW && sandbox.getTrapConfig().isEnabled(TrapFlag.OBJECT_ALLOC, TrapFlag.PRE_OBJECT_ALLOC)){
            // TODO - insert trap call
            super.visitLdcInsn(makeTrapSite(TrapSite.Position.PRE));

        }else if(opcode == Opcodes.ANEWARRAY && sandbox.getTrapConfig().isEnabled(TrapFlag.ARRAY_ALLOC, TrapFlag.PRE_ARRAY_ALLOC)){
            // TODO - insert trap call
        }
        super.visitTypeInsn(opcode, type);
        if(opcode == Opcodes.NEW && sandbox.getTrapConfig().isEnabled(TrapFlag.OBJECT_ALLOC, TrapFlag.POST_OBJECT_ALLOC)){
            // TODO - insert trap call
        }else if(opcode == Opcodes.ANEWARRAY && sandbox.getTrapConfig().isEnabled(TrapFlag.ARRAY_ALLOC, TrapFlag.POST_ARRAY_ALLOC)){
            // TODO - insert trap call
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions){
        if(sandbox.getTrapConfig().isEnabled(TrapFlag.ARRAY_ALLOC, TrapFlag.PRE_ARRAY_ALLOC)){
            // TODO - insert trap call
        }
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
        if(sandbox.getTrapConfig().isEnabled(TrapFlag.ARRAY_ALLOC, TrapFlag.POST_ARRAY_ALLOC)){
            // TODO - insert trap call
        }
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface){
        if(sandbox.getTrapConfig().isEnabled(TrapFlag.METHOD_CALL, TrapFlag.PRE_METHOD_CALL)){
            // TODO - insert trap call
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if(sandbox.getTrapConfig().isEnabled(TrapFlag.METHOD_CALL, TrapFlag.POST_METHOD_CALL)){
            // TODO - insert trap call
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

        var uncaughtEntry = guardedBlocks.stream()
                .filter(t -> t.sameBlock(tc.start(), tc.end()))
                .findFirst()
                .get();

        super.visitLabel(uncaughtEntry.handler());
        visitInsn(Opcodes.ATHROW);
        if(uncaughtEntry.chainHandler() != null){
            super.visitLabel(uncaughtEntry.chainHandler());
        }

        unmarkGuardedBlock(tc.start(), tc.end());
    }

    protected void generateBackJumpTrap(TrapSite.Position pos){
        super.visitLdcInsn(makeTrapSite(pos));

    }

    protected ConstantDynamic makeTrapSite(TrapSite.Position pos){

        var paramNames = Arrays.stream(Type.getArgumentTypes(sandbox.getMethodDescriptor()))
                .map(Type::getClassName)
                .toArray(String[]::new);

        return new ConstantDynamic("trap$" + (trap++),
                Type.getDescriptor(TrapSite.class),
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        Type.getInternalName(JvmSandboxingVisitor.class),
                        "bootstrapTrapsite",
                        Type.getMethodDescriptor(
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

    protected static TrapSite bootstrapTrapsite(TrapSite.Position pos, String className, String method, String returnType, String[] params, int line) throws ClassNotFoundException {
        var signature = new Class<?>[params.length + 1];
        signature[0] = classForName(returnType);

        for(int i = 1; i < params.length; i++){
            signature[i] = classForName(params[i - 1]);
        }

        return new TrapSite(pos, className, method, signature, line);
    }

    protected static Class<?> classForName(String name) throws ClassNotFoundException {
        // TODO - arrays

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

}

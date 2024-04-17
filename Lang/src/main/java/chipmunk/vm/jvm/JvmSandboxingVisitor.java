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

import chipmunk.vm.invoke.security.LinkingPolicy;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JvmSandboxingVisitor extends MethodVisitor {

    public record TryCatch(Label start, Label end, Label handler, Label chainHandler){

        public boolean sameBlock(Label s, Label e){
            return start == s && end == e;
        }

    }

    public enum TrapFlags {
        BACK_JUMP,
        METHOD_CALL,
        RECURSIVE_CALL
    }

    protected final LinkingPolicy linkage;
    protected final List<Label> visited;
    protected final List<TryCatch> guardedBlocks;

    public JvmSandboxingVisitor(MethodVisitor delegate, LinkingPolicy linkage){
        super(Opcodes.ASM9, delegate);
        this.linkage = linkage;

        visited = new ArrayList<>();
        guardedBlocks = new ArrayList<>();
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label){
        if(labelVisited(label)){
            // Label has already been visited, so this is a backjump
            // TODO - insert trap call for backjumps
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

}

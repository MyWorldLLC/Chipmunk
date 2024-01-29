package chipmunk.vm.jvm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class TrapTransformer extends MethodVisitor {
    public TrapTransformer(int api, final MethodVisitor delegate) {
        super(api, delegate);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type){
        // TODO - handle NEW, ANEWARRAY traps
    }

    @Override
    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions){

    }

    @Override
    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface){
        // TODO - handle method call traps
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label){

    }

    @Override
    public void visitLabel(final Label label){

    }

}

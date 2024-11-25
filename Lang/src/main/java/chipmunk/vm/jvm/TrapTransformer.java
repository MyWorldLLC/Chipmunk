package chipmunk.vm.jvm;

import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.TrapHandler;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.Set;

public class TrapTransformer extends MethodVisitor {

    protected final String methodName;
    protected final Type declaringClass;
    protected final Type methodDescriptor;
    protected final Set<Label> alreadySeen;
    protected int lineNumber;

    public TrapTransformer(int api, final MethodVisitor delegate, String methodName, Type declaringClass, Type methodDescriptor) {
        super(api, delegate);

        this.methodName = methodName;
        this.declaringClass = declaringClass;
        this.methodDescriptor = methodDescriptor;

        alreadySeen = new HashSet<>();
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
        if(alreadySeen.contains(label)){
            // Backjump, emit trap call

            emitGetTrapHandler();
            emitTrapSite(TrapSite.Position.PRE);

            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(TrapHandler.class),
                    "backJump",
                    Type.getMethodType(Type.VOID_TYPE, Type.getType(TrapSite.class)).getDescriptor(),
                    true
            );
        }
    }

    @Override
    public void visitLabel(final Label label){
        alreadySeen.add(label);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        lineNumber = line;
        super.visitLineNumber(line, start);
    }

    protected void emitGetTrapHandler() {
        super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(ChipmunkScript.class),
                "getCurrentTrapHandler",
                Type.getMethodType(Type.getType(TrapHandler.class)).getInternalName(),
                false
        );
    }

    protected void emitTrapSite(TrapSite.Position position){

        // TODO - replace inline trapsite creation with constants (is indy applicable here?)
        super.visitTypeInsn(Opcodes.NEW, Type.getInternalName(TrapSite.class));

        super.visitFieldInsn(Opcodes.GETSTATIC,
                Type.getInternalName(TrapSite.Position.class),
                position.name(),
                Type.getDescriptor(TrapSite.Position.class));

        emitMethodIdentifier(declaringClass.getInternalName(), methodName, methodDescriptor);

        super.visitLdcInsn(lineNumber);

        super.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getInternalName(TrapSite.class),
                "<init>",
                Type.getMethodType(Type.VOID_TYPE,
                        Type.getType(TrapSite.Position.class),
                        Type.getType(MethodIdentifier.class),
                        Type.getType(int.class))
                        .getDescriptor(),
                false);
    }

    protected void emitMethodIdentifier(String cls, String methodName, Type methodDescriptor){
        super.visitTypeInsn(Opcodes.NEW, Type.getType(MethodIdentifier.class).getInternalName());

        super.visitLdcInsn(cls); // TODO - load class
        super.visitLdcInsn(methodName);
        super.visitLdcInsn(methodDescriptor.getDescriptor()); // TODO - load signature classes

        super.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getInternalName(MethodIdentifier.class),
                "<init>",
                Type.getMethodType(Type.VOID_TYPE,
                                Type.getType(Class.class),
                                Type.getType(String.class),
                                Type.getType(Class[].class))
                        .getDescriptor(),
                false);
    }
}

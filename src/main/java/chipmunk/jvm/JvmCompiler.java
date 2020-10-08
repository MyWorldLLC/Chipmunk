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

import chipmunk.ChipmunkVM;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CMethodCode;
import chipmunk.modules.runtime.CModule;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class JvmCompiler {

    private final ChipmunkClassLoader loader;

    public JvmCompiler(){
        loader = new ChipmunkClassLoader();
    }

    public Object compile(CModule module){

        Type objType = Type.getType(Object.class);

        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, module.getName(), null, objType.getInternalName(), null);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, objType.getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        // TODO - generate field & methods in bytecode
        for(String symbol : module.getNamespace().names()){
            int flags = Opcodes.ACC_PUBLIC;
            if(module.getNamespace().finalNames() != null && module.getNamespace().finalNames().contains(symbol)){
                flags |= Opcodes.ACC_FINAL;
            }

            Object value = module.getNamespace().get(symbol);
            if(value instanceof CMethod){
                visitMethod(cw, flags, module.getName(), symbol, (CMethod) value);
            }else{
                visitVar(cw, flags, symbol);
            }
        }

        // TODO - generate module bytecode initializer method
        // The module initializer must run before anything else because it sets all Chipmunk bytecode fields

        // TODO - generate the normal module initializer method
        // This is the usual module initializer that runs all module-level variable expressions & runs the
        // CClass shared initializers

        byte[] bytes = cw.toByteArray();

        return instantiate(loadClass(module.getName(), bytes));
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
        cw.visitField(flags, name, type.getDescriptor(), null, null);
    }

    protected void visitMethod(ClassWriter cw, int flags, String className, String name, CMethod method){
        // Create a field to hold the bytecode
        visitVar(cw, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "cm$" + name, Type.getType(CMethodCode.class));

        Type methodType = Type.getMethodType(Type.getType(Object.class), Type.getType(ChipmunkVM.class), Type.getType(Object[].class));

        // Generate a method of the form name(vm, params) that contains the bytecode for the expression
        // return vm.dispatch(Class.cm$name, params);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | flags, name, methodType.getDescriptor(), null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.GETSTATIC, className, "cm$" + name, Type.getType(CMethodCode.class).getDescriptor());
        mv.visitVarInsn(Opcodes.ALOAD, 2);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkVM.class).getInternalName(),
                "dispatch",
                Type.getMethodType(Type.getType(Object.class), Type.getType(CMethodCode.class), Type.getType(Object[].class)).getDescriptor(),
                false);

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(4, 3);

        mv.visitEnd();

    }
}

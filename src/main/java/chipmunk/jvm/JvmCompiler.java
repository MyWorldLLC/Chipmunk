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
import chipmunk.binary.*;
import chipmunk.invoke.Binder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class JvmCompiler {

    private final ChipmunkClassLoader loader;

    protected final CompiledMethods methods;

    public JvmCompiler(){
        loader = new ChipmunkClassLoader();
        methods = new CompiledMethods();
    }

    public CompiledModule compile(BinaryModule module){

        Type objType = Type.getType(Object.class);

        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, module.getName(), null, objType.getInternalName(), new String[]{Type.getType(CompiledModule.class).getInternalName()});

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, objType.getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        for(BinaryNamespace.Entry entry : module.getNamespace()){
            int flags = Opcodes.ACC_PUBLIC;
            if(BinaryConstants.isFlagSet(entry.getFlags(), BinaryConstants.FINAL_FLAG)){
                flags |= Opcodes.ACC_FINAL;
            }

            if(entry.getType() == FieldType.METHOD){
                visitMethod(cw, flags, module.getName(), entry.getName(), entry.getBinaryMethod());
            }else{
                visitVar(cw, flags, entry.getName());
            }
        }

        // TODO - generate module bytecode initializer method & make class implement the CompiledModule interface
        // The module initializer must run before anything else because it sets all Chipmunk bytecode fields

        // TODO - generate the normal module initializer method
        // This is the usual module initializer that runs all module-level variable expressions & runs the
        // CClass shared initializers

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
        cw.visitField(flags, name, type.getDescriptor(), null, null);
    }

    protected void visitMethod(ClassWriter cw, int flags, String className, String name, BinaryMethod method){
        // Create a field to hold the bytecode
        visitVar(cw, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "cm$" + name, Type.getType(BinaryMethod.class));

        Type methodType = Type.getMethodType(Type.getType(Object.class), Type.getType(ChipmunkVM.class), Type.getType(Object[].class));

        // Generate a method of the form name(vm, params) that contains the bytecode for the expression
        // return vm.dispatch(receiver, Class.cm$name, params);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | flags, name, methodType.getDescriptor(), null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.GETSTATIC, className, "cm$" + name, Type.getType(BinaryMethod.class).getDescriptor());
        mv.visitVarInsn(Opcodes.ALOAD, 2);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkVM.class).getInternalName(),
                "dispatch",
                Type.getMethodType(Type.getType(Object.class), Type.getType(Object.class), Type.getType(BinaryMethod.class), Type.getType(Object[].class)).getDescriptor(),
                false);

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(4, 3);

        mv.visitEnd();

    }

    protected void generateDynamicInvocation(ClassWriter cw, MethodVisitor mv, String method, int paramCount) {
        final Type objType = Type.getType(Object.class);

        Type[] pTypes = new Type[paramCount];
        for(int i = 0; i < pTypes.length; i++){
            pTypes[i] = objType;
        }

        Type callType = Type.getMethodType(objType, pTypes);

        mv.visitMethodInsn(Opcodes.INVOKEDYNAMIC,
                Type.getType(Binder.class).getInternalName(),
                "bootstrapCallsite",
                callType.getDescriptor(),
                false);
    }
}

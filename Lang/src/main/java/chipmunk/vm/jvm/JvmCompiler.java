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

package chipmunk.vm.jvm;

import chipmunk.ChipmunkRuntimeException;
import chipmunk.compiler.ModuleNotFoundException;
import chipmunk.runtime.*;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.ChipmunkVM;
import chipmunk.binary.DebugEntry;
import chipmunk.compiler.assembler.InvalidOpcodeChipmunk;
import chipmunk.binary.*;
import chipmunk.vm.ModuleLoader;
import chipmunk.vm.invoke.Binder;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static chipmunk.compiler.assembler.Opcodes.*;

public class JvmCompiler {

    protected final JvmCompilerConfig config;

    public JvmCompiler(){
        this(new JvmCompilerConfig());
    }

    public JvmCompiler(JvmCompilerConfig config){
        this.config = config;
    }

    public JvmCompilerConfig getConfig(){
        return config;
    }

    public ChipmunkScript compile(CompilationUnit sources) throws IOException, BinaryFormatException {

        BinaryModule mainBin = sources.getModuleLoader().loadBinary(sources.getEntryModule());
        if(mainBin == null){
            throw new ModuleNotFoundException("Could not find main module " + sources.getEntryModule());
        }

        Type scriptType = Type.getType(ChipmunkScript.class);

        ClassWriter sw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        sw.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, "ChipmunkScriptImpl", null, scriptType.getInternalName(),
                null);
        sw.visitSource(mainBin.getFileName(), null);

        // Generate constructor
        MethodVisitor constructor = sw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getType(ChipmunkScript.class).getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();

        // Generate run() method
        MethodVisitor run = sw.visitMethod(Opcodes.ACC_PUBLIC, "run",
                Type.getMethodType(Type.getType(Object.class), Type.getType(Object[].class)).getDescriptor(),
                null, null);

        run.visitCode();

        // Make this script current
        run.visitVarInsn(Opcodes.ALOAD, 0);
        run.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getType(ChipmunkScript.class).getInternalName(),
                "setCurrentScript",
                Type.getMethodType(Type.VOID_TYPE, Type.getType(ChipmunkScript.class)).getDescriptor(),
                false);

        // Get the module instance
        run.visitVarInsn(Opcodes.ALOAD, 0);
        run.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkScript.class).getInternalName(),
                "getVM",
                Type.getMethodType(Type.getType(ChipmunkVM.class)).getDescriptor(),
                false);
        run.visitInsn(Opcodes.DUP);
        run.visitVarInsn(Opcodes.ALOAD, 0);
        run.visitLdcInsn(mainBin.getName());
        run.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkVM.class).getInternalName(),
                "getModule",
                Type.getMethodType(
                        Type.getType(ChipmunkModule.class),
                        Type.getType(ChipmunkScript.class),
                        Type.getType(String.class)).getDescriptor(),
                false);

        // Invoke the main method with run() parameters
        run.visitLdcInsn(sources.getEntryMethodName());
        run.visitVarInsn(Opcodes.ALOAD, 1);
        run.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkVM.class).getInternalName(),
                "invoke",
                Type.getMethodType(
                        Type.getType(Object.class),
                        Type.getType(Object.class),
                        Type.getType(String.class),
                        Type.getType(Object[].class)).getDescriptor(),
                false);
        run.visitInsn(Opcodes.ARETURN);


        run.visitMaxs(0, 0);
        run.visitEnd();

        sw.visitEnd();

        return (ChipmunkScript) instantiate(sources.getModuleLoader().getClassLoader().define("ChipmunkScriptImpl", sw.toByteArray()));
    }

    public ChipmunkModule compileModule(BinaryModule module){
        return compileModule(new JvmCompilation(module, new ModuleLoader()));
    }

    public ChipmunkModule compileModule(JvmCompilation compilation){

        final BinaryModule module = compilation.getModule();

        Type objType = Type.getType(Object.class);

        ClassWriter moduleWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        moduleWriter.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, jvmName(compilation.getPrefixedModuleName()), null, objType.getInternalName(),
                new String[]{
                        Type.getInternalName(ChipmunkModule.class)
                });
        moduleWriter.visitSource(module.getFileName(), null);
        moduleWriter.visitAnnotation(Type.getDescriptor(AllowChipmunkLinkage.class), true).visitEnd();

        // Implement ChipmunkModule interface

        // ChipmunkModule.getName()
        MethodVisitor getName = moduleWriter.visitMethod(Opcodes.ACC_PUBLIC, "getName", Type.getMethodType(Type.getType(String.class)).getDescriptor(), null, null);
        getName.visitCode();
        getName.visitLdcInsn(compilation.getPrefixedModuleName());
        getName.visitInsn(Opcodes.ARETURN);
        getName.visitMaxs(0, 0);
        getName.visitEnd();

        // ChipmunkModule.initialize(ChipmunkVM)
        MethodVisitor initialize = moduleWriter.visitMethod(Opcodes.ACC_PUBLIC, "initialize", Type.getMethodType(Type.VOID_TYPE, Type.getType(ChipmunkVM.class)).getDescriptor(), null, null);
        initialize.visitCode();
        if(module.getNamespace().has("$module_init$")){
            initialize.visitVarInsn(Opcodes.ALOAD, 0);
            initialize.visitVarInsn(Opcodes.ALOAD, 1);
            initialize.visitMethodInsn(Opcodes.INVOKEVIRTUAL, jvmName(compilation.getPrefixedModuleName()), "$module_init$", Type.getMethodType(Type.getType(Object.class), Type.getType(Object.class)).getDescriptor(), false);
            initialize.visitInsn(Opcodes.POP);
        }
        initialize.visitInsn(Opcodes.RETURN);
        initialize.visitMaxs(0, 0);
        initialize.visitEnd();


        // ChipmunkModule.getDepedencies()
        /*moduleWriter.visitField(Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, "$depedencies",
                Type.getType(String[].class).getDescriptor(), null, null);

        MethodVisitor getDependencies = moduleWriter.visitMethod(Opcodes.ACC_PUBLIC, "getDependencies", Type.getMethodType(Type.getType(String[].class)).getDescriptor(), null, null);
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
        MethodVisitor moduleInit = moduleWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
        moduleInit.visitCode();
        moduleInit.visitVarInsn(Opcodes.ALOAD, 0);
        moduleInit.visitMethodInsn(Opcodes.INVOKESPECIAL, objType.getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);

        final NamespaceInfo moduleInfo = new NamespaceInfo(moduleWriter, moduleInit, compilation.getPrefixedModuleName());
        compilation.enterNamespace(moduleInfo);
        visitNamespace(compilation, module.getNamespace());

        moduleInit.visitInsn(Opcodes.RETURN);
        moduleInit.visitMaxs(0, 0);
        moduleInit.visitEnd();

        moduleWriter.visitEnd();

        byte[] bytes = moduleWriter.toByteArray();

        return (ChipmunkModule) instantiate(loadClass(compilation.getLoader().getClassLoader(), compilation.getPrefixedModuleName(), bytes));
    }

    protected Class<?> loadClass(ChipmunkClassLoader loader, String name, byte[] bytes){
        return loader.define(name, bytes);
    }

    protected <T> T instantiate(Class<T> cls){
        try {
            return cls.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected void visitVar(JvmCompilation compilation, int flags, String name){
        visitVar(compilation, flags, name, Type.getType(Object.class));
    }

    protected void visitVar(JvmCompilation compilation, int flags, String name, Type type){
        ClassWriter cw = compilation.containingNamespace().getWriter();
        cw.visitField(flags, name, type.getDescriptor(), null, null).visitEnd();
    }

    protected Class<?> visitChipmunkClass(JvmCompilation compilation, BinaryClass cls){

        final String className = cls.getName();
        final String qualifiedInsName = compilation.qualifiedContainingName() + "." + className;
        final String qualifiedCClassName = compilation.qualifiedContainingName() + "." + className + "$class";

        ClassWriter cClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cClassWriter.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, jvmName(qualifiedCClassName), null, Type.getInternalName(Object.class),
                new String[]{
                        Type.getInternalName(ChipmunkClass.class)
                });
        cClassWriter.visitSource(cls.getModule().getFileName(), null);
        cClassWriter.visitAnnotation(Type.getDescriptor(AllowChipmunkLinkage.class), true).visitEnd();

        // Generate class constructor
        MethodVisitor clsConstructor = cClassWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
        clsConstructor.visitCode();
        clsConstructor.visitVarInsn(Opcodes.ALOAD, 0);
        clsConstructor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);

        final NamespaceInfo clsNamespace = new NamespaceInfo(cClassWriter, clsConstructor, className + "$class");
        compilation.enterNamespace(clsNamespace);

        // Before running class initializer, fill the traits
        // Shared traits
        visitTraits(compilation, "$sharedTraits", cls.getSharedNamespace());

        // Instance traits
        visitTraits(compilation, "$traits", cls.getInstanceNamespace());

        // Generate class namespace
        visitNamespace(compilation, cls.getSharedNamespace());
        compilation.exitNamespace();

        if(cls.getSharedNamespace().has("$class_init$")){
            clsConstructor.visitVarInsn(Opcodes.ALOAD, 0);
            clsConstructor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, jvmName(qualifiedCClassName), "$class_init$", Type.getMethodType(Type.getType(Object.class)).getDescriptor(), false);
        }

        ClassWriter cInsWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cInsWriter.visit(Opcodes.V14, Opcodes.ACC_PUBLIC, jvmName(qualifiedInsName), null, Type.getInternalName(Object.class), new String[]{
                Type.getInternalName(ChipmunkObject.class)
        });
        cInsWriter.visitSource(cls.getModule().getFileName(), null);
        cInsWriter.visitAnnotation(Type.getDescriptor(AllowChipmunkLinkage.class), true).visitEnd();

        // Generate class field
        cInsWriter.visitField(Opcodes.ACC_PUBLIC, "$cClass", Type.getType(ChipmunkClass.class).getDescriptor(), null, null)
                .visitEnd();

        // Generate instance constructor
        final String constructorName = "$" + className;
        BinaryMethod binaryConstructor = cls.getInstanceNamespace().getEntry(constructorName).getBinaryMethod();

        // The self parameter is always implicit and is not reflected in the binaryConstructor.getArgCount() call.
        // The <init> params then are:
        // 0: self
        // 1: cClass
        // 2+: binaryConstructor params
        Type[] initTypes = paramTypes(binaryConstructor.getArgCount() + 1);
        initTypes[0] = Type.getObjectType(jvmName(qualifiedCClassName));

        // The constructor params are:
        // 0: self
        // 1+: binaryConstructor params
        Type[] constructorTypes = paramTypes(binaryConstructor.getArgCount());

        MethodVisitor insConstructor = cInsWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE, initTypes).getDescriptor(), null, null);
        insConstructor.visitCode();
        insConstructor.visitVarInsn(Opcodes.ALOAD, 0);
        insConstructor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);

        // Generate instance namespace
        final NamespaceInfo insNamespace = new NamespaceInfo(cInsWriter, insConstructor, className);
        compilation.enterNamespace(insNamespace);
        visitNamespace(compilation, cls.getInstanceNamespace());

        // Generate toString() that Java recognizes
        if(cls.getInstanceNamespace().has("toString")
                && cls.getInstanceNamespace().getEntry("toString").getType() == FieldType.METHOD){

            MethodVisitor toString = cInsWriter.visitMethod(Opcodes.ACC_PUBLIC, "toString", Type.getMethodType(Type.getType(String.class)).getDescriptor(), null, null);
            toString.visitCode();

            // Invoke the toString() method defined in Chipmunk code
            toString.visitVarInsn(Opcodes.ALOAD, 0);
            toString.visitMethodInsn(Opcodes.INVOKEVIRTUAL, jvmName(qualifiedInsName), "toString", Type.getMethodType(Type.getType(Object.class)).getDescriptor(), false);
            toString.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(String.class).getInternalName());
            toString.visitInsn(Opcodes.ARETURN);

            toString.visitMaxs(0, 0);
            toString.visitEnd();

        }
        compilation.exitNamespace();

        // Store the $cClass
        insConstructor.visitVarInsn(Opcodes.ALOAD, 0);
        insConstructor.visitVarInsn(Opcodes.ALOAD, 1);
        insConstructor.visitFieldInsn(Opcodes.PUTFIELD, jvmName(qualifiedInsName), "$cClass", Type.getType(ChipmunkClass.class).getDescriptor());

        // Invoke $instance_init$
        insConstructor.visitVarInsn(Opcodes.ALOAD, 0);
        insConstructor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, jvmName(qualifiedInsName), "$instance_init$", Type.getMethodType(Type.getType(Object.class)).getDescriptor(), false);

        // Invoke constructor
        insConstructor.visitVarInsn(Opcodes.ALOAD, 0);
        for(int i = 0; i < constructorTypes.length; i++){
            insConstructor.visitVarInsn(Opcodes.ALOAD, i + 2);
        }
        insConstructor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, jvmName(qualifiedInsName), constructorName, Type.getMethodType(Type.getType(Object.class), constructorTypes).getDescriptor(), false);


        // Close constructors *after* visiting the class namespaces
        clsConstructor.visitInsn(Opcodes.RETURN);
        clsConstructor.visitMaxs(0, 0);
        clsConstructor.visitEnd();

        insConstructor.visitInsn(Opcodes.RETURN);
        insConstructor.visitMaxs(0, 0);
        insConstructor.visitEnd();

        // Create new method on the class object to create instances of the instance class
        // newInstance = CClass.new(p1, p2, ...)

        // cClass.new params are:
        // 0: self (cClass)
        // 1+: binaryConstructor params

        Type insType = Type.getObjectType(jvmName(qualifiedInsName));
        Type newMethodType = Type.getMethodType(insType, constructorTypes);

        MethodVisitor callMethod = cClassWriter.visitMethod(Opcodes.ACC_PUBLIC, "new", newMethodType.getDescriptor(), null, null);
        callMethod.visitCode();
        callMethod.visitTypeInsn(Opcodes.NEW, insType.getInternalName());
        callMethod.visitInsn(Opcodes.DUP);
        callMethod.visitVarInsn(Opcodes.ALOAD, 0);
        for(int i = 0; i < newMethodType.getArgumentTypes().length; i++){
            callMethod.visitVarInsn(Opcodes.ALOAD, i + 1);
        }
        callMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, insType.getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE, initTypes).getDescriptor(), false);
        callMethod.visitInsn(Opcodes.ARETURN);
        callMethod.visitMaxs(0, 0);
        callMethod.visitEnd();

        // Generate CClass.getModule()
        MethodVisitor cClassGetModule = cClassWriter.visitMethod(Opcodes.ACC_PUBLIC, "getModule", Type.getMethodType(Type.getType(ChipmunkModule.class)).getDescriptor(), null, null);
        cClassGetModule.visitCode();
        // TODO - pass module to cClass constructor?
        cClassGetModule.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getType(ChipmunkScript.class).getInternalName(),
                "getCurrentScript",
                Type.getMethodType(Type.getType(ChipmunkScript.class)).getDescriptor(),
                false);
        cClassGetModule.visitInsn(Opcodes.DUP);
        cClassGetModule.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkScript.class).getInternalName(),
                "getVM",
                Type.getMethodType(Type.getType(ChipmunkVM.class)).getDescriptor(),
                false);
        cClassGetModule.visitInsn(Opcodes.SWAP);
        cClassGetModule.visitLdcInsn(compilation.qualifiedContainingName());
        cClassGetModule.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkVM.class).getInternalName(),
                "getModule",
                Type.getMethodType(
                        Type.getType(ChipmunkModule.class),
                        Type.getType(ChipmunkScript.class),
                        Type.getType(String.class)).getDescriptor(),
                false);
        cClassGetModule.visitInsn(Opcodes.ARETURN);
        cClassGetModule.visitMaxs(0, 0);
        cClassGetModule.visitEnd();

        // Generate cClass.getTraits()
        MethodVisitor getTraits = cClassWriter.visitMethod(Opcodes.ACC_PUBLIC, "getTraits", Type.getMethodType(Type.getType(TraitField[].class)).getDescriptor(), null, null);
        getTraits.visitCode();
        getTraits.visitVarInsn(Opcodes.ALOAD, 0);
        getTraits.visitFieldInsn(Opcodes.GETFIELD, jvmName(qualifiedCClassName), "$traits", Type.getDescriptor(TraitField[].class));
        getTraits.visitInsn(Opcodes.ARETURN);
        getTraits.visitMaxs(0, 0);
        getTraits.visitEnd();

        // Generate cClass.getSharedTraits()
        MethodVisitor getSharedTraits = cClassWriter.visitMethod(Opcodes.ACC_PUBLIC, "getSharedTraits", Type.getMethodType(Type.getType(TraitField[].class)).getDescriptor(), null, null);
        getSharedTraits.visitCode();
        getSharedTraits.visitVarInsn(Opcodes.ALOAD, 0);
        getSharedTraits.visitFieldInsn(Opcodes.GETFIELD, jvmName(qualifiedCClassName), "$sharedTraits", Type.getDescriptor(TraitField[].class));
        getSharedTraits.visitInsn(Opcodes.ARETURN);
        getSharedTraits.visitMaxs(0, 0);
        getSharedTraits.visitEnd();

        cClassWriter.visitEnd();

        // Implement ChipmunkObject.getChipmunkClass()
        MethodVisitor insGetCClass = cInsWriter.visitMethod(Opcodes.ACC_PUBLIC, "getChipmunkClass", Type.getMethodType(Type.getType(ChipmunkClass.class)).getDescriptor(), null, null);
        insGetCClass.visitVarInsn(Opcodes.ALOAD, 0);
        insGetCClass.visitFieldInsn(Opcodes.GETFIELD, jvmName(qualifiedInsName), "$cClass", Type.getType(ChipmunkClass.class).getDescriptor());
        insGetCClass.visitInsn(Opcodes.ARETURN);
        insGetCClass.visitMaxs(0, 0);
        insGetCClass.visitEnd();

        cInsWriter.visitEnd();

        Class<?> cClass = loadClass(compilation.getLoader().getClassLoader(), qualifiedCClassName, cClassWriter.toByteArray());
        loadClass(compilation.getLoader().getClassLoader(), qualifiedInsName, cInsWriter.toByteArray());

        return cClass;
    }

    protected void visitNamespace(JvmCompilation compilation, BinaryNamespace ns){
        final NamespaceInfo namespaceInfo = compilation.containingNamespace();

        final ClassWriter writer = namespaceInfo.getWriter();
        final MethodVisitor constructor = namespaceInfo.getInit();

        for(BinaryNamespace.Entry entry : ns){
            int flags = Opcodes.ACC_PUBLIC;
            if(BinaryConstants.isFlagSet(entry.getFlags(), BinaryConstants.FINAL_FLAG)){
                flags |= Opcodes.ACC_FINAL;
            }

            if(entry.getType() == FieldType.METHOD){
                visitMethod(compilation, namespaceInfo.getWriter(), flags, entry.getName(), entry.getBinaryMethod());
            }else if(entry.getType() == FieldType.CLASS){

                // Generate class
                Class<?> compiledCClass = visitChipmunkClass(compilation, entry.getBinaryClass());
                Type clsType = Type.getType(compiledCClass);

                // Add field to parent class
                writer.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, entry.getName(),
                        Type.getType(compiledCClass).getDescriptor(), null, null);

                // Add call to construct class instance
                constructor.visitVarInsn(Opcodes.ALOAD, 0);
                constructor.visitTypeInsn(Opcodes.NEW, clsType.getInternalName());
                constructor.visitInsn(Opcodes.DUP);
                constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, clsType.getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), false);
                constructor.visitFieldInsn(Opcodes.PUTFIELD, jvmName(compilation.qualifiedContainingName()), entry.getName(), clsType.getDescriptor());

            }else{
                visitVar(compilation, flags, entry.getName());
            }
        }
    }

    protected void visitTraits(JvmCompilation compilation, String fieldName, BinaryNamespace namespace){
        final NamespaceInfo info = compilation.containingNamespace();
        ClassWriter cls = info.getWriter();
        MethodVisitor init = info.getInit();
        String typeName = jvmName(compilation.qualifiedContainingName());

        cls.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, fieldName, Type.getDescriptor(TraitField[].class), null, null)
                .visitEnd();

        List<String> traitNames = namespace.getEntries()
                .stream()
                .filter(e -> (e.getFlags() & BinaryConstants.TRAIT_FLAG) != 0)
                .map(BinaryNamespace.Entry::getName)
                .collect(Collectors.toList());

        if(!traitNames.isEmpty()){
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitLdcInsn(traitNames.size());
            init.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(TraitField.class));

            for(int i = 0; i < traitNames.size(); i++){
                init.visitInsn(Opcodes.DUP);
                init.visitLdcInsn(i);
                // Instantiate trait field
                init.visitTypeInsn(Opcodes.NEW, Type.getInternalName(TraitField.class));
                init.visitInsn(Opcodes.DUP);
                init.visitLdcInsn(traitNames.get(i));
                init.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(TraitField.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)), false);
                // Store trait field
                init.visitInsn(Opcodes.AASTORE);
            }

            init.visitFieldInsn(Opcodes.PUTFIELD, typeName, fieldName, Type.getDescriptor(TraitField[].class));
        }

    }

    protected void visitMethod(JvmCompilation compilation, ClassWriter cw, int flags, String name, BinaryMethod method){

        final Type objType = Type.getType(Object.class);

        Type[] pTypes = new Type[Math.max(0, method.getArgCount())];
        Arrays.fill(pTypes, objType);

        Type methodType = Type.getMethodType(objType, pTypes);

        MethodVisitor mv = cw.visitMethod(flags, name, methodType.getDescriptor(), null, null);
        mv.visitCode();

        Map<Integer, Label> labelMappings = new HashMap<>();
        DebugEntry[] debugTable = method.getDebugTable();
        int debugIndex = 0;

        ExceptionBlock[] exceptionTable = method.getExceptionTable();
        int exceptionIndex = 0;


        byte[] instructions = method.getCode();
        for(int ip = 0; ip < instructions.length;) {

            // Visit labels before generating bytecode
            Label label = markLabel(ip, labelMappings);
            mv.visitLabel(label);

            // Generate debug data
            if(debugTable != null && debugIndex < debugTable.length){
                DebugEntry entry = debugTable[debugIndex];
                // This isn't technically correct but it prevents crashes if the debug table is malformed
                if(entry.endIndex <= ip && debugIndex + 1 < debugTable.length){
                    debugIndex++;
                    entry = debugTable[debugIndex];
                }
                mv.visitLineNumber(entry.lineNumber, label);
            }

            // Generate exception handlers
            if(exceptionTable != null && exceptionIndex < exceptionTable.length){
                ExceptionBlock ex = exceptionTable[exceptionIndex];
                if(ex.startIndex == ip){
                    Label end = markLabel(ex.endIndex, labelMappings);
                    Label handler = markLabel(ex.catchIndex, labelMappings);
                    mv.visitTryCatchBlock(label, end, handler, Type.getType(Exception.class).getInternalName());
                    exceptionIndex++;
                }
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
                    int jumpTarget = fetchInt(instructions, ip + 1);
                    if(jumpTarget < ip && !config.areBackjumpChecksDisabled()){
                        generateBackjumpCheckpoint(config.getYieldType(), mv);
                    }
                    generateGoto(mv, jumpTarget, labelMappings);
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
                case SWAP -> {
                    generateSwap(mv);
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

        Type[] pTypes = new Type[set ? 2 : 1];
        pTypes[0] = objType;
        if(set){
            pTypes[1] = objType;
        }

        Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC,
                Type.getType(Binder.class).getInternalName(),
                set ? Binder.INDY_BOOTSTRAP_SET : Binder.INDY_BOOTSTRAP_GET,
                Binder.bootstrapFieldOpType().toMethodDescriptorString(),
                false);

        mv.visitInvokeDynamicInsn(field, Type.getMethodType(objType, pTypes).getDescriptor(), bootstrap);

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

        // Test if the object on the stack is a Throwable or not.
        // If not, wrap in an AngryChipmunk & throw, else throw unmodified.

        mv.visitInsn(Opcodes.DUP);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, Type.getType(Throwable.class).getInternalName());
        Label endWrap = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, endWrap);

        mv.visitTypeInsn(Opcodes.NEW, Type.getType(ChipmunkRuntimeException.class).getInternalName());
        mv.visitInsn(Opcodes.DUP_X1);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getType(ChipmunkRuntimeException.class).getInternalName(),
                "<init>",
                Type.getMethodType(Type.VOID_TYPE, Type.getType(Object.class)).getDescriptor(),
                false);

        mv.visitLabel(endWrap);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(Throwable.class).getInternalName());
        mv.visitInsn(Opcodes.ATHROW);

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
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getInternalName(Null.class),
                    "getInstance",
                    Type.getMethodType(Type.getType(Null.class)).getDescriptor(),
                    false);
        }
    }

    protected void generateDup(MethodVisitor mv){
        mv.visitInsn(Opcodes.DUP);
    }

    protected void generateSwap(MethodVisitor mv){
        mv.visitInsn(Opcodes.SWAP);
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

    protected void generateBackjumpCheckpoint(JvmCompilerConfig.YieldType yieldType, MethodVisitor mv){
        // Get the executing script instance
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getType(ChipmunkScript.class).getInternalName(),
                "getCurrentScript",
                Type.getMethodType(Type.getType(ChipmunkScript.class)).getDescriptor(),
                false);

        // Get the yield status
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getType(ChipmunkScript.class).getInternalName(),
                "isYielded",
                Type.getMethodType(Type.BOOLEAN_TYPE).getDescriptor(),
                false);

        // Generate the yield guard
        Label ifEnd = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, ifEnd);

        // Generate the yield
        switch (yieldType) {
            case THREAD_YIELD -> {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        Type.getType(Thread.class).getInternalName(),
                        "yield",
                        Type.getMethodType(Type.VOID_TYPE).getDescriptor(),
                        false);
            }
            case FORCED_PREEMPT -> {
                mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(ForcedYieldThrowable.class));
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                        Type.getInternalName(ForcedYieldThrowable.class),
                        "<init>",
                        Type.getMethodType(Type.VOID_TYPE).getDescriptor(),
                        false);
                mv.visitInsn(Opcodes.ATHROW);
            }
        }

        // Close the yield guard
        mv.visitLabel(ifEnd);
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

    public String jvmName(String moduleName){
        return moduleName.replace('.', '/');
    }

    public String cClassName(String className){
        return jvmName(className) + "$class";
    }

    public Type methodCallType(BinaryMethod m){
        Type objType = Type.getType(Object.class);
        return Type.getMethodType(objType, paramTypes(m));
    }

    public Type[] paramTypes(BinaryMethod m){
        return paramTypes(Math.max(0, m.getArgCount() - 1)); // JVM doesn't count self as a parameter
    }

    public Type[] paramTypes(int params){
        Type objType = Type.getType(Object.class);

        Type[] pTypes = new Type[params];
        Arrays.fill(pTypes, objType);
        return pTypes;
    }
}

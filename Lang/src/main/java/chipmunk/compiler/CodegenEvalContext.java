/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.compiler;

import chipmunk.compiler.ir.LocalBlockNode;
import chipmunk.compiler.ir.VarDecNode;
import chipmunk.compiler.ir.blocks.ClassNode;
import chipmunk.compiler.ir.blocks.MethodNode;
import chipmunk.compiler.ir.blocks.ModuleNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.MethodType;
import chipmunk.compiler.types.ObjectType;
import chipmunk.runtime.MethodBinding;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.*;

import static java.lang.constant.ConstantDescs.*;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;

public class CodegenEvalContext implements EvaluationContext {

    protected final Compilation compilation;
    protected final EvaluationEnvironment env;
    protected final Deque<CodeEvaluator> evaluators;
    protected final Deque<ClassBuilder> classBuilders;

    protected final Map<ObjectType, ClassDesc> typeMapping;

    protected final Map<String, byte[]> classes;

    public CodegenEvalContext(Compilation compilation, EvaluationEnvironment env) {
        this.compilation = compilation;
        this.env = env;

        evaluators = new ArrayDeque<>();
        classBuilders = new ArrayDeque<>();
        classes = new HashMap<>();

        typeMapping = new IdentityHashMap<>();
        initBuiltinTypes();
    }

    public Map<String, byte[]> getEmittedClasses(){
        return Collections.unmodifiableMap(classes);
    }

    public Optional<Variable> lookupVariable(String varName){
        if(evaluators.isEmpty()){
            throw new IllegalStateException("Not currently assembling a method. This is a compiler bug.");
        }
        return evaluators.peek().localScope().lookupVariable(varName);
    }

    public void evaluateModule(ModuleNode module){
        var name = prefixedClassName(module.moduleType().name());
        var descriptor = ClassDesc.of(name);

        var code = ClassFile.of()
                .build(descriptor, builder -> {
                    classBuilders.push(builder);
                    newClass(builder, name, ModuleNode.INITIALIZER_NAME);
                    module.evaluate(env, this);
                    exitModule(module);
                });
        classes.put(name, code);
    }

    protected void exitModule(ModuleNode module){
        classBuilders.pop();
    }

    public void evaluateClass(ClassNode classNode) {

        var name = prefixedClassName(classNode.classType().name());

        var code = ClassFile.of()
                .build(ClassDesc.of(name), builder -> {
                    classBuilders.push(builder);
                    classNode.evaluate(env, this);
                    exitClass(classNode);
                });
        classes.put(name, code);
    }

    protected void exitClass(ClassNode classNode) {
        classBuilders.pop();
    }

    public void evaluateMethod(MethodNode method){
        if(classBuilders.isEmpty()){
        }
        var builder = classBuilders.peek();
        var methodType = method.methodType();
        builder.withMethodBody(method.name(), methodDescriptorFor(methodType), ClassFile.ACC_PUBLIC, code -> {
            evaluators.push(new CodeEvaluator(this, code));
            enterLocalScope(method);
            method.evaluate(env, this);
            exitMethod();
        });
    }

    @Override
    public void evaluateVarDec(VarDecNode varDec) {
        // TODO
    }

    @Override
    public CodeEvaluator codeEvaluator() {
        return evaluators.peek();
    }

    protected void exitMethod(){
        exitLocalScope();
        evaluators.pop();
    }

    public void enterLocalScope(LocalBlockNode scope){
        if(evaluators.isEmpty()){
            throw new IllegalStateException("Not currently assembling a method. This is a compiler bug.");
        }
        evaluators.peek().enterLocalScope(scope);
    }

    public void exitLocalScope(){
        if(evaluators.isEmpty()){
            throw new IllegalStateException("Not currently assembling a method. This is a compiler bug.");
        }
        evaluators.peek().exitLocalScope();
    }

    public MethodTypeDesc methodDescriptorFor(MethodType methodType){
        // We have to skip 1 when generating the JVM descriptor to account for the fact that "self" is in the AST/IR
        // but not in the JVM's descriptor.
        return MethodTypeDesc.of(descriptorFor(methodType.rType()), methodType.pTypes().stream().skip(1).map(this::descriptorFor).toList());
    }

    protected String prefixedClassName(String name){
        if(compilation.getCompilerConfig().packagePrefix() != null){
            return compilation.getCompilerConfig().packagePrefix() + "." + name;
        }
        return name;
    }

    private void initBuiltinTypes(){
        typeMapping.put(BuiltinTypes.VOID, CD_void);
        typeMapping.put(BuiltinTypes.ANY, CD_Object);
        typeMapping.put(BuiltinTypes.BOOLEAN, CD_boolean);
        typeMapping.put(BuiltinTypes.BYTE, CD_byte);
        typeMapping.put(BuiltinTypes.SHORT, CD_short);
        typeMapping.put(BuiltinTypes.INT, CD_int);
        typeMapping.put(BuiltinTypes.LONG, CD_long);
        typeMapping.put(BuiltinTypes.FLOAT, CD_float);
        typeMapping.put(BuiltinTypes.DOUBLE, CD_double);
        typeMapping.put(BuiltinTypes.STRING, CD_String);
        typeMapping.put(BuiltinTypes.LIST, descriptorFor(Map.class));
        typeMapping.put(BuiltinTypes.MAP, descriptorFor(List.class));
    }

    protected ClassDesc descriptorFor(Class<?> cls){
        if(cls.isPrimitive()){
            var mapping = new HashMap<Class<?>, ClassDesc>();
            mapping.put(boolean.class, CD_boolean);
            mapping.put(byte.class, CD_byte);
            mapping.put(short.class, CD_short);
            mapping.put(int.class, CD_int);
            mapping.put(long.class, CD_long);
            mapping.put(float.class, CD_float);
            mapping.put(double.class, CD_double);
            return mapping.get(cls);
        }
        return ClassDesc.of(cls.getName());
    }

    protected ClassDesc descriptorFor(ObjectType type){
        if(type == null){
            return descriptorFor(BuiltinTypes.ANY);
        }
        if(type instanceof chipmunk.compiler.types.MethodType){
            return descriptorFor(MethodBinding.class);
        }
        var desc = typeMapping.get(type);
        if(desc == null){
            desc = ClassDesc.of(type.name()); // TODO - qualified & package-prefixed names
            typeMapping.put(type, desc);
        }
        return desc;
    }

    protected void newClass(ClassBuilder builder, String name, String... initMethods){
        var descriptor = ClassDesc.of(name);
        builder.withFlags(AccessFlag.PUBLIC)
                .withMethodBody(INIT_NAME, MTD_void,
                        ClassFile.ACC_PUBLIC,
                        init -> {
                            init.aload(0)
                                    .invokespecial(CD_Object,
                                            INIT_NAME, MTD_void);
                            for(var initMethod : initMethods){
                                init.aload(0)
                                        .invokevirtual(descriptor, initMethod, MethodTypeDesc.of(CD_void));
                            }

                            init.return_();
                        });
    }
}

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

package chipmunk.compiler.ir.passes;

import chipmunk.compiler.CodeEvaluator;
import chipmunk.compiler.Compilation;
import chipmunk.compiler.Intrinsics;
import chipmunk.compiler.Variable;
import chipmunk.compiler.ir.IRNode;
import chipmunk.compiler.ir.blocks.LocalBlockNode;
import chipmunk.compiler.ir.blocks.ClassNode;
import chipmunk.compiler.ir.blocks.MethodNode;
import chipmunk.compiler.ir.blocks.ModuleNode;
import chipmunk.compiler.ir.blocks.SyntheticMethodNode;
import chipmunk.compiler.ir.expression.ExpressionNode;
import chipmunk.compiler.ir.expression.OperationNode;
import chipmunk.compiler.types.*;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.runtime.MethodBinding;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import static java.lang.constant.ConstantDescs.MTD_void;

public class EvaluationContext {

    private record ClassAssembler(ClassBuilder builder, Queue<MethodNode> lambdas){
        public static ClassAssembler create(ClassBuilder builder){
            return new ClassAssembler(builder, new ArrayDeque<>());
        }
    }

    protected final Compilation compilation;
    protected final EvaluationEnvironment env;
    protected final Deque<CodeEvaluator> evaluators;
    protected final Deque<ClassAssembler> classBuilders;

    protected final Map<ObjectType, ClassDesc> typeMapping;

    protected final Map<String, byte[]> classes;

    protected boolean isEvaluatingLambdas;

    public EvaluationContext(Compilation compilation, EvaluationEnvironment env) {
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
                    classBuilders.push(ClassAssembler.create(builder));
                    newClass(builder, name, ModuleNode.INITIALIZER_NAME);
                    builder.withInterfaceSymbols(ClassDesc.of(ChipmunkModule.class.getName()));
                    module.evaluate(env, this);
                    exitModule(module);
                });
        classes.put(name, code);
    }

    protected void exitModule(ModuleNode module){
        flushLambdas();
        classBuilders.pop();
    }

    public void evaluateClass(ClassNode classNode) {

        var name = prefixedClassName(classNode.classType().name());

        var code = ClassFile.of()
                .build(ClassDesc.of(name), builder -> {
                    classBuilders.push(ClassAssembler.create(builder));
                    classNode.evaluate(env, this);
                    exitClass(classNode);
                });
        classes.put(name, code);
    }

    protected void exitClass(ClassNode classNode) {
        flushLambdas();
        classBuilders.pop();
    }

    public void evaluateMethod(MethodNode method){
        evaluateMethod(method, false);
    }

    protected void evaluateMethod(MethodNode method, boolean lambdaPhase){
        if(method.isLambda() && !lambdaPhase){
            enqueueLambda(method);
        }else{
            if(classBuilders.isEmpty()){
                throw new IllegalArgumentException("Not currently evaluating a class. This is a compiler bug.");
            }
            var builder = classBuilders.peek().builder();
            var methodType = method.methodType();

            builder.withMethodBody(method.name(), methodDescriptorFor(methodType), ClassFile.ACC_PUBLIC, code -> {
                evaluators.push(new CodeEvaluator(this, code));
                method.evaluate(env, this);
            });
        }
    }

    /**
     * Synthetic methods are methods that are necessary for JVM interop or other under-the-hood functionality,
     * but don't exist in the IR (such as module/class initializers). Note that the code evaluator used here has
     * no ability to resolve local variables.
     */
    public void writeSyntheticMethod(String name, MethodType methodType, Consumer<CodeEvaluator> builder){
        if(classBuilders.isEmpty()){
            throw new IllegalArgumentException("Not currently evaluating a class. This is a compiler bug.");
        }
        var clsBuilder = classBuilders.peek().builder();
        clsBuilder.withMethodBody(name, methodDescriptorFor(methodType), ClassFile.ACC_PUBLIC,
                code -> {
                    var evaluator = new CodeEvaluator(this, code);
                    evaluator.enterLocalScope(new SyntheticMethodNode());
                    builder.accept(evaluator);
                    evaluator.exitLocalScope();
                });
    }

    public void makeBranch(ExpressionNode condition, Label skipLabel){
        if(condition instanceof OperationNode op && op.children().size() == 2){
            // TODO - support unary branch intrinsics
            var left = op.children().getFirst();
            var right = op.children().getLast();

            var lType = left.inferredType();
            var rType = right.inferredType();

            if(rType.isAssignableTo(lType)){
                var emitter = Intrinsics.getBranch(op.operationName(), lType);
                if(emitter.isPresent()){
                    left.evaluate(env, this);
                    right.evaluate(env, this);
                    emitter.get().emitter().accept(codeEvaluator().builder(), skipLabel);
                    return;
                }
            }
        }

        // Fallback to generic branch
        condition.evaluate(env, this);
        checkAndConvert(condition.inferredType(), BuiltinTypes.BOOLEAN);

        codeEvaluator().ifeq(skipLabel);
    }

    public void storeLocal(IRNode codeSite, String name, ObjectType type){
        var scope = codeSite.lookupVariableScope(name).orElseThrow(() -> new IllegalStateException("Cannot find scope for variable " + name));
        var index = scope.variables().indexOf(name);
        var variable = scope.variables().get(index);
        checkAndConvert(type, variable.type());
        // TODO - check upvalue flag
        codeEvaluator().setLocal(index, variable.type());
    }

    public void loadLocal(IRNode codeSite, String name, ObjectType type){
        var scope = codeSite.lookupVariableScope(name).orElseThrow(() -> new IllegalStateException("Cannot find scope for variable " + name));
        var index = scope.variables().indexOf(name);
        var variable = scope.variables().get(index);

        var storedType = variable.type();
        codeEvaluator().getLocal(index, storedType);
        // TODO - check upvalue flag
        checkAndConvert(storedType, type);
    }

    public void pushZeroValue(ObjectType type){
        var code = codeEvaluator();
        switch (type){
            case BooleanType _ -> code.push(0);
            case IntegerType i -> {
                switch (i.bitSize()){
                    case 8 -> code.push((byte) 0);
                    case 16 -> code.push((short) 0);
                    case 32 -> code.push(0);
                    case 64 -> code.push((long)0);
                }
            }
            case FloatType f -> {
                switch (f.bitSize()){
                    case 32 -> code.push(0.0f);
                    case 64 -> code.push(0.0d);
                }
            }
            case VoidType _ -> throw new IllegalArgumentException("Cannot push void to stack. This is a compiler bug.");
            default -> code.pushNull();
        }
    }

    public CodeEvaluator codeEvaluator() {
        return evaluators.peek();
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

    public void checkAndConvert(ObjectType actual, ObjectType expected){
        if(!actual.isAssignableTo(expected)){
            if(actual.canPromoteTo(expected)){
                Intrinsics.getConversion(expected, actual)
                        .ifPresentOrElse(emitter -> evaluators.peek().conversion(actual, expected, emitter),
                                () -> {
                                    throw new IllegalArgumentException("Cannot convert " + actual + " to " + expected);
                                });
            }else{
                throw new IllegalArgumentException("Cannot convert " + actual + " to " + expected + ". This is a compiler bug.");
            }
        }
    }

    public ObjectType[] evaluateAndConvertAll(List<IRNode> nodes, List<ObjectType> expectedTypes){
        if(nodes.size() != expectedTypes.size()){
            throw new IllegalArgumentException("Mismatched conversion sizes");
        }
        var convertedTypes = new ObjectType[nodes.size()];
        for(int i = 0; i < nodes.size(); i++){
            var node = nodes.get(i);
            node.evaluate(env, this);
            checkAndConvert(node.inferredType(), expectedTypes.get(i));
            convertedTypes[i] = expectedTypes.get(i);
        }
        return convertedTypes;
    }

    public boolean isEvaluatingLambdas(){
        return isEvaluatingLambdas;
    }

    public void enqueueLambda(MethodNode lambda){
        classBuilders.peek().lambdas().add(lambda);
    }

    protected void flushLambdas(){
        isEvaluatingLambdas = true;
        var assembler = classBuilders.peek();
        while(!assembler.lambdas().isEmpty()){
            var lambda = assembler.lambdas().poll();
            evaluateMethod(lambda, true);
        }
        isEvaluatingLambdas = false;
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
        typeMapping.put(BuiltinTypes.LIST, descriptorFor(List.class));
        typeMapping.put(BuiltinTypes.MAP, descriptorFor(Map.class));
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

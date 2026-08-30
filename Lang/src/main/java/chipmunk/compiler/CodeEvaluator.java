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

import chipmunk.compiler.ir.blocks.LocalBlockNode;
import chipmunk.compiler.ir.passes.EvaluationContext;
import chipmunk.compiler.types.*;
import chipmunk.runtime.MethodBinding;
import chipmunk.vm.invoke.Binder;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.*;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.constant.ConstantDescs.*;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;

public class CodeEvaluator {

    public record BlockLabels(Label start, Label end, boolean isLoop) {}

    protected final Stack stack = new Stack();
    protected final CodeBuilder code;
    protected final EvaluationContext ctx;

    protected final Deque<BlockLabels> blockLabels;

    protected LocalBlockNode localScope;

    private final Map<ObjectType, ClassDesc> typeMapping;

    public CodeEvaluator(EvaluationContext ctx, CodeBuilder code){
        this.ctx = ctx;
        this.code = code;
        typeMapping = new IdentityHashMap<>();
        blockLabels = new ArrayDeque<>();
        initBuiltinTypes();
    }

    public void enterLocalScope(LocalBlockNode scope){
        this.localScope = scope;
    }

    public LocalBlockNode localScope(){
        return localScope;
    }

    public void exitLocalScope(){
        var parent = localScope.parent();
        if(parent instanceof LocalBlockNode l){
            localScope = l;
        }else{
            localScope = null;
        }
    }

    public CodeEvaluator push(boolean b){
        stack.push(BuiltinTypes.BOOLEAN);
        code.loadConstant(b ? 1 : 0);
        return this;
    }

    public CodeEvaluator push(byte b){
        stack.push(BuiltinTypes.BYTE);
        code.loadConstant(b);
        return this;
    }

    public CodeEvaluator push(short s){
        stack.push(BuiltinTypes.SHORT);
        code.loadConstant(s);
        return this;
    }

    public CodeEvaluator push(int i){
        stack.push(BuiltinTypes.INT);
        code.loadConstant(i);
        return this;
    }

    public CodeEvaluator push(long l){
        stack.push(BuiltinTypes.LONG);
        code.loadConstant(l);
        return this;
    }

    public CodeEvaluator push(float f){
        stack.push(BuiltinTypes.FLOAT);
        code.loadConstant(f);
        return this;
    }

    public CodeEvaluator push(double d){
        stack.push(BuiltinTypes.DOUBLE);
        code.loadConstant(d);
        return this;
    }

    public CodeEvaluator push(String s){
        stack.push(BuiltinTypes.STRING);
        code.loadConstant(s);
        return this;
    }

    public CodeEvaluator pushNull(){
        stack.push(BuiltinTypes.ANY);
        code.aconst_null();
        return this;
    }

    public CodeEvaluator add(ObjectType type){
        stack.doOperation(() -> emitOp("+", type, type), type, type);
        return this;
    }

    public CodeEvaluator sub(ObjectType type){
        stack.doOperation(() -> emitOp("-", type, type), type, type);
        return this;
    }

    public CodeEvaluator mul(ObjectType type){
        stack.doOperation(() -> emitOp("*", type, type), type, type);
        return this;
    }

    public CodeEvaluator power(ObjectType type){
        stack.doOperation(() -> emitOp("**", type, type), type, type);
        return this;
    }

    public CodeEvaluator div(ObjectType type){
        stack.doOperation(() -> emitOp("/", type, type), type, type);
        return this;
    }

    public CodeEvaluator fdiv(ObjectType type){
        stack.doOperation(() -> emitOp("//", type, type), type, type);
        return this;
    }

    public CodeEvaluator mod(ObjectType type){
        stack.doOperation(() -> emitOp("%", type, type), type, type);
        return this;
    }

    public CodeEvaluator inc(ObjectType type){
        stack.doOperation(() -> emitOp("++", type), type);
        return this;
    }

    public CodeEvaluator dec(ObjectType type){
        stack.doOperation(() -> emitOp("--", type), type);
        return this;
    }

    public CodeEvaluator bitNeg(ObjectType type){
        stack.doOperation(() -> emitOp("~", type, type), type, type);
        return this;
    }

    public CodeEvaluator bitAnd(ObjectType type){
        stack.doOperation(() -> emitOp("&", type, type), type, type);
        return this;
    }

    public CodeEvaluator bitOr(ObjectType type){
        stack.doOperation(() -> emitOp("|", type, type), type, type);
        return this;
    }

    public CodeEvaluator bitXor(ObjectType type){
        stack.doOperation(() -> emitOp("^", type, type), type, type);
        return this;
    }

    public CodeEvaluator lshift(ObjectType type){
        stack.doOperation(() -> emitOp("<<", type, type), type, type);
        return this;
    }

    public CodeEvaluator rshift(ObjectType type){
        stack.doOperation(() -> emitOp(">>", type, type), type, type);
        return this;
    }

    public CodeEvaluator urshift(ObjectType type){
        stack.doOperation(() -> emitOp(">>>", type, type), type, type);
        return this;
    }

    public CodeEvaluator boolAnd(ObjectType type){
        stack.doOperation(() -> emitOp("&&", type, type), type, type);
        return this;
    }

    public CodeEvaluator boolOr(ObjectType type){
        stack.doOperation(() -> emitOp("||", type, type), type, type);
        return this;
    }

    public CodeEvaluator boolNot(ObjectType type){
        stack.doOperation(() -> emitOp("!", type), type);
        return this;
    }

    public CodeEvaluator lt(ObjectType type){
        stack.doOperation(() -> emitOp("<", type, type), type, type);
        return this;
    }

    public CodeEvaluator lte(ObjectType type){
        stack.doOperation(() -> emitOp("<=", type, type), type, type);
        return this;
    }

    public CodeEvaluator gt(ObjectType type){
        stack.doOperation(() -> emitOp(">", type, type), type, type);
        return this;
    }

    public CodeEvaluator gte(ObjectType type){
        stack.doOperation(() -> emitOp(">=", type, type), type, type);
        return this;
    }

    public CodeEvaluator eq(ObjectType type){
        stack.doOperation(() -> emitOp("==", type, type), type, type);
        return this;
    }

    public CodeEvaluator neq(ObjectType type){
        stack.doOperation(() -> emitOp("!=", type, type), type, type);
        return this;
    }

    public CodeEvaluator is(ObjectType type){
        stack.doOperation(() -> emitOp("is", type, type), type, type);
        return this;
    }

    public CodeEvaluator getAt(ObjectType type){
        stack.doOperation(() -> emitOp("getAt", type, type), type, type);
        return this;
    }

    public CodeEvaluator setAt(ObjectType type){
        stack.doOperation(() -> emitOp("setAt", type, type), type, type);
        return this;
    }

    public CodeEvaluator instanceOf(ObjectType type, String clsName){
        // TODO - need to make sure this is only called for reference types or
        // do something sensible for primitives (such as compile-time evaluation).
        stack.pop();
        code.instanceOf(ClassDesc.of(clsName));
        return this;
    }

    public CodeEvaluator setLocal(int index, ObjectType type){
        stack.pop();
        code.storeLocal(typeKind(type), index);
        return this;
    }

    public CodeEvaluator getLocal(int index, ObjectType type){
        stack.push(type);
        code.loadLocal(typeKind(type), index);
        return this;
    }

    public CodeEvaluator newInstance(ObjectType type, Class<?> cls, ObjectType... params){
        return newInstance(type, cls.getName(), params);
    }

    public CodeEvaluator newInstance(ObjectType rType, String clsName, ObjectType... params){
        stack.doOperation(rType, params);
        var target = ClassDesc.of(clsName);
        code.new_(target)
                .dup()
                .invokespecial(target, INIT_NAME, methodDescriptor(BuiltinTypes.VOID, params));
        return this;
    }

    public CodeEvaluator invokeVirtual(ObjectType type, Class<?> cls, String method, ObjectType... params){
        return invokeVirtual(type, cls.getName(), method, params);
    }

    public CodeEvaluator invokeVirtual(ObjectType rType, String clsName, String method, ObjectType... params){
        stack.doOperation(rType, params);
        var target = ClassDesc.of(clsName);
        code.invokevirtual(target, method, methodDescriptor(rType, params));
        return this;
    }

    public CodeEvaluator invokeInterface(ObjectType type, Class<?> cls, String method, ObjectType... params){
        return invokeInterface(type, cls.getName(), method, params);
    }

    public CodeEvaluator invokeInterface(ObjectType rType, String clsName, String method, ObjectType... params){
        stack.doOperation(rType, params);
        code.invokeinterface(ClassDesc.of(clsName), method, methodDescriptor(rType, params));
        return this;
    }

    public CodeEvaluator pop(){
        stack.pop();
        code.pop();
        return this;
    }

    public CodeEvaluator dup(){
        stack.dup();
        code.dup();
        return this;
    }

    public CodeEvaluator _return(ObjectType type){
        switch (type){
            case VoidType _ -> code.return_();
            case BooleanType _ -> code.ireturn();
            case IntegerType i -> {
                switch (i.bitSize()){
                    case 8, 16, 32 -> code.ireturn();
                    case 64 -> code.lreturn();
                }
            }
            case FloatType f -> {
                switch (f.bitSize()){
                    case 32 -> code.freturn();
                    case 64 -> code.dreturn();
                }
            }
            default -> code.areturn();
        }
        return this;
    }

    public CodeEvaluator _goto(Label target){
        code.goto_(target);
        return this;
    }

    public ObjectType operation(String op, ObjectType... types){
        return stack.doOperation(() -> emitOp(op, types), types);
    }

    public CodeEvaluator conversion(ObjectType from, ObjectType to, ConversionEmitter conversion){
        stack.doOperation(to, from);
        conversion.emitter().accept(code);
        return this;
    }

    public CodeEvaluator ifeq(Label skipLabel){
        code.ifeq(skipLabel);
        return this;
    }

    public void makeLoop(Consumer<CodeBuilder.BlockCodeBuilder> builder){
        makeBlock(true, builder);
    }

    public void makeBlock(Consumer<CodeBuilder.BlockCodeBuilder> builder){
        makeBlock(false, builder);
    }

    public void makeBlock(boolean isLoop, Consumer<CodeBuilder.BlockCodeBuilder> builder){
        code.block(block -> {
            blockLabels.push(new BlockLabels(block.startLabel(), block.endLabel(), isLoop));
            builder.accept(block);
            blockLabels.pop();
        });
    }

    public CodeEvaluator _break(){
        code.goto_(nearestLoop().end());
        return this;
    }

    public CodeEvaluator _continue(){
        code.goto_(nearestLoop().start());
        return this;
    }

    public CodeBuilder builder(){
        return code;
    }

    public Optional<OpEmitter> getOp(String symbol, ObjectType... types){
        return Intrinsics.getEmitter(symbol, types);
    }

    protected ObjectType emitOp(String symbol, ObjectType... types){
        var intrinsic = getOp(symbol, types);
        return intrinsic.map(emitter -> {
            emitter.emitter().accept(code);
            return emitter.op().rValue();
        }).orElseGet(() -> {
            genDynamicInvocation(code, symbol, types);
            return BuiltinTypes.ANY;
        });
    }

    public int emitSafepointStore(SymbolStorage<Variable> locals){
        // TODO - get stack and local depth/types, init frame,
        // emit code to save the stack/locals to the frame,
        // and throw an uncatchable yield exception to further unwind the stack
        return 0; // TODO - return the id of this safepoint
    }

    public CodeEvaluator emitSafepointRestore(int id){
        // TODO - emit code to get stack from executing fiber,
        // restore stack & locals, and jump to this safepoint's target label
        return this;
    }

    private void genDynamicInvocation(CodeBuilder code, String op, ClassDesc... argTypes){
        var objType = ClassDesc.of(Object.class.getName());

        var dynamicOp = binaryOpNames(op);

        var callType = MethodTypeDesc.of(objType, argTypes);

        var CD_Binder = ClassDesc.of(Binder.class.getName());
        var CD_CallSite = ClassDesc.of(CallSite.class.getName());
        var CD_MHLookup = ClassDesc.of(MethodHandles.Lookup.class.getName());
        var CD_MType = ClassDesc.of(java.lang.invoke.MethodType.class.getName());
        var bootstrapDescriptor = MethodTypeDesc.of(CD_CallSite, CD_MHLookup, CD_String, CD_MType).descriptorString();

        code.invokedynamic(DynamicCallSiteDesc.of(
                MethodHandleDesc.of(DirectMethodHandleDesc.Kind.STATIC, CD_Binder,
                        Binder.INDY_BOOTSTRAP_METHOD, bootstrapDescriptor), dynamicOp, callType));
    }

    private void genDynamicInvocation(CodeBuilder code, String op, ObjectType... argTypes){
        var pTypes = Arrays.stream(argTypes)
                .map(this::descriptorFor)
                .toArray(ClassDesc[]::new);
        genDynamicInvocation(code, op, pTypes);
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
            desc = ClassDesc.of(type.name()); // TODO - qualified names
            typeMapping.put(type, desc);
        }
        return desc;
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

    private String binaryOpNames(String op){
        return switch(op){
            case "+" -> "plus";
            case "-" -> "minus";
            case "*" -> "mul";
            case "/" -> "div";
            case "//" -> "fdiv";
            case "%" -> "mod";
            case "pow" -> "mul";
            case "^" -> "binaryXor";
            case "&" -> "binaryAnd";
            case "|" -> "binaryOr";
            case "<<" -> "lShift";
            case ">>" -> "rShift";
            case ">>>" -> "unsignedRShift";
            case "==" -> "equals";
            case "<", ">", "<=", ">=" -> "compare";
            default -> op;
        };
    }

    private TypeKind typeKind(ObjectType t){
        return switch (t){
            case BooleanType _ -> TypeKind.BOOLEAN;
            case IntegerType i ->
                    switch (i.bitSize()){
                        case 8 -> TypeKind.BYTE;
                        case 16 -> TypeKind.SHORT;
                        case 32 -> TypeKind.INT;
                        case 64 -> TypeKind.LONG;
                        default -> TypeKind.LONG;
                    };
            case FloatType f ->
                    switch (f.bitSize()){
                        case 32 -> TypeKind.FLOAT;
                        case 64 -> TypeKind.DOUBLE;
                        default -> TypeKind.DOUBLE;
                    };
            default -> TypeKind.REFERENCE;
        };
    }

    protected BlockLabels nearestLoop(){
        for(var block : blockLabels){
            if(block.isLoop()){
                return block;
            }
        }
        return null;
    }

    protected MethodTypeDesc methodDescriptor(ObjectType rType, ObjectType... pTypes){
        return MethodTypeDesc.of(descriptorFor(rType), Arrays.stream(pTypes).map(this::descriptorFor).toArray(ClassDesc[]::new));
    }

}

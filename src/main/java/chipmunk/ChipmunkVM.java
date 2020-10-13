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

package chipmunk;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import chipmunk.binary.BinaryMethod;
import chipmunk.binary.BinaryModule;
import chipmunk.binary.BinaryNamespace;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.invoke.*;
import chipmunk.jvm.CompiledModule;
import chipmunk.jvm.JvmCompiler;
import chipmunk.modules.runtime.*;

import static chipmunk.Opcodes.*;

public class ChipmunkVM {

	public enum SecurityMode {
		UNRESTRICTED, SANDBOXED
	}

	public class CallFrame {
		public final BinaryMethod method;
		public final int ip;
		public final Object[] locals;
		public final OperandStack stack;

		public CallFrame(BinaryMethod method, int ip, Object[] locals, OperandStack stack) {
			this.method = method;
			this.ip = ip;
			this.locals = locals;
			this.stack = stack;
		}
	}

	protected List<ModuleLoader> loaders;
	protected ChipmunkScript activeScript;
	protected Map<String, BinaryModule> modules;

	protected Deque<CallFrame> frozenCallStack;
	
	public volatile boolean interrupted;

	protected SecurityMode securityMode;
	private int memHigh;

	private final CBoolean trueValue;
	private final CBoolean falseValue;
	
	private final int refLength;

	protected final Binder binder;
	protected final JvmCompiler jvmCompiler;

	public ChipmunkVM() {
		this(SecurityMode.SANDBOXED);
	}

	public ChipmunkVM(SecurityMode securityMode) {

		this.securityMode = securityMode;
		activeScript = new ChipmunkScript(128);
		frozenCallStack = activeScript.frozenCallStack;

		memHigh = 0;

		trueValue = new CBoolean(true);
		falseValue = new CBoolean(false);

		refLength = 8; // assume 64-bit references

		binder = new Binder();
		jvmCompiler = new JvmCompiler();
	}

	public SecurityMode getSecurityMode(){
		return securityMode;
	}

	public void setSecurityMode(SecurityMode mode){
		securityMode = mode;
	}
	
	public List<ModuleLoader> getLoaders(){
		return activeScript.getLoaders();
	}

	public ChipmunkScript getActiveScript(){
		return activeScript;
	}
	
	public BinaryModule loadModule(String moduleName) throws ModuleLoadChipmunk {
		
		if(modules.containsKey(moduleName)){
			// this module is already loaded - skip
			return modules.get(moduleName);
		}
		
		for(ModuleLoader loader : loaders){
			try {
				BinaryModule module = loader.loadModule(moduleName);
				if(module != null){
					// need to record the module *before* handling imports in case
					// of a circular import
					modules.put(module.getName(), module);
					
					return module;
				}
			}catch(Exception e){
				throw new ModuleLoadChipmunk(e);
			}
		}
		
		throw new ModuleLoadChipmunk(String.format("Module %s not found", moduleName));
	}

	public BinaryModule getModule(String name) {
		return modules.get(name);
	}

	public BinaryModule resolveModule(String name) throws ModuleLoadChipmunk {
		if(!modules.containsKey(name)){
			loadModule(name);
		}
		return modules.get(name);
	}
	
	public void freeze(BinaryMethod method, int ip, Object[] locals, OperandStack stack) {
		frozenCallStack.push(new CallFrame(method, ip, locals, stack));
	}

	public CallFrame unfreezeNext() {
		return frozenCallStack.pop();
	}
	
	public boolean hasNextFrame(){
		return !frozenCallStack.isEmpty();
	}

	public static ChipmunkScript compile(CharSequence src, String scriptName) throws CompileChipmunk {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule[] modules = compiler.compile(src, scriptName);
		return modulesToScript(modules, scriptName);
	}

	public static ChipmunkScript compile(InputStream is, String scriptName) throws CompileChipmunk {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule[] modules = compiler.compile(is, scriptName);
		return modulesToScript(modules, scriptName);
	}

	private static ChipmunkScript modulesToScript(BinaryModule[] modules, String scriptName) {

		BinaryModule mainModule = null;
		for (BinaryModule module : modules) {
			if (module.getNamespace().has("main")) {
				mainModule = module;
				break;
			}
		}

		if (mainModule == null) {
			throw new IllegalArgumentException("Script contains no main method");
		}

		ChipmunkScript script = new ChipmunkScript();
		script.setEntryCall(mainModule.getName(), "main");

		MemoryModuleLoader loader = new MemoryModuleLoader();
		//loader.addModule(ChipmunkModuleBuilder.buildLangModule());
		loader.addModules(Arrays.asList(modules));
		script.getLoaders().add(loader);

		return script;
	}

	public static Object run(InputStream is, String scriptName) throws CompileChipmunk {
		
		ChipmunkScript script = compile(is, scriptName);
		ChipmunkVM vm = new ChipmunkVM();
		
		return vm.run(script);
	}

	public Object run(ChipmunkScript script) throws AngryChipmunk {
		
		interrupted = false;

		activeScript = script;

		loaders = script.getLoaders();
		modules = script.getModules();
		frozenCallStack = script.frozenCallStack;

		if(!activeScript.isInitialized()){
			loadModule(script.entryModule);

			BinaryModule entryModule = activeScript.modules.get(activeScript.entryModule);
			//if(entryModule.hasInitializer()){
				// The entry module has an initializer that must run before the entry method.
				// Push a frozen call frame to invoke it.

				BinaryMethod initializer = entryModule.getInitializer();
				Object[] initLocals = new Object[initializer.getLocalCount() + 1];
				//initLocals[0] = initializer.getSelf();

				OperandStack initStack = new OperandStack();
				freeze(initializer, 0, initLocals, initStack);
			//}

			BinaryMethod entryMethod = (BinaryMethod) entryModule.getNamespace().get(activeScript.entryMethod);

			Object[] entryLocals = new Object[entryMethod.getLocalCount() + 1];
			//entryLocals[0] = entryMethod.getSelf();
			for (int i = 0; activeScript.entryArgs != null && i < activeScript.entryArgs.length; i++) {
				entryLocals[i + 1] = activeScript.entryArgs[i];
			}

			OperandStack entryStack = new OperandStack();

			freeze(entryMethod, 0, entryLocals, entryStack);

			activeScript.markInitialized();
		}

		if(hasNextFrame()){
			// If frozen call stack isn't empty,
			// continue dispatch
			return this.dispatch(frozenCallStack.peek().method, null);
		}

		return null;
	}

	public void interrupt(){
		interrupted = true;
	}

	public void forceSuspend() throws SuspendedChipmunk {
		interrupt();
		throw new SuspendedChipmunk();
	}

	public void traceMem(int newlyAllocated) {
		memHigh += newlyAllocated;
	}
	
	public void untraceMem(int amount) {
		memHigh -= amount;
	}

	public void traceBoolean() {
		memHigh += 1;
	}
	
	public CBoolean traceBoolean(boolean value) {
		traceBoolean();
		return new CBoolean(value);
	}
	
	public void untraceBoolean() {
		memHigh -= 1;
	}

	public void traceInteger() {
		memHigh += 4;
	}
	
	public CInteger traceInteger(int value) {
		traceInteger();
		return new CInteger(value);
	}
	
	public void untraceInteger() {
		memHigh -= 4;
	}

	public void traceFloat() {
		memHigh += 4;
	}
	
	public CFloat traceFloat(float value) {
		traceFloat();
		return new CFloat(value);
	}
	
	public void untraceFloat() {
		memHigh -= 4;
	}

	public String traceString(String str) {
		memHigh += str.length() * 2;
		return str;
	}
	
	public void traceReference() {
		memHigh += refLength;
	}
	
	public Object traceReference(Object obj) {
		traceReference();
		return obj;
	}
	
	public void untraceReference() {
		memHigh -= refLength;
	}
	
	public void untraceReferences(int count) {
		memHigh -= refLength * count;
	}

	public void checkArity(Object[] params, int arity) throws IllegalArgumentException {
		if(params == null && arity != 0){
			throw new IllegalArgumentException("Parameter array is null");
		}

		if(params.length != arity){
			throw new IllegalArgumentException(String.format("Expected %d parameters, was passed %d", arity, params.length));
		}
	}
	
//	public void dumpOperandStack(OperandStack stack) {
//		System.out.println("Stack depth: " + stackDepth(stack));
//		for(int i = 0; i < stackDepth(stack); i++) {
//			System.out.println(stack.stack[i].toString());
//		}
//	}
//	
//	public int stackDepth(OperandStack stack) {
//		return stack.stackIndex;
//	}

	public Object dispatch(BinaryMethod code, Object[] parameters){
		// Receiverless dispatch - static methods
		return null;
	}

	public Object dispatch(Object receiver, BinaryMethod code, Object[] parameters){
		return null;
	}

	public Object dispatch(CMethod method, Object[] parameters) {

		int ip = 0;
		Object[] locals;
		OperandStack stack;
		
		final byte[] instructions = method.getCode().getCode();
		final int localCount = method.getCode().getLocalCount();
		final Object[] constantPool = method.getCode().getConstantPool();

		if (frozenCallStack.size() > 0) {
			CallFrame frame = unfreezeNext();
			ip = frame.ip;
			locals = frame.locals;
			stack = frame.stack;

			// call into the next method to resume call stack
			try {
				if(frozenCallStack.size() > 0){
					stack.push(this.dispatch(frozenCallStack.peek().method, null));
				}
			} catch (SuspendedChipmunk e) {
				this.freeze(frame.method, ip, locals, stack);
				throw e;
			} catch (Exception e) {
				// handle exception - fill in trace or jump to handler
				if (!(e instanceof AngryChipmunk)) {
					e = new AngryChipmunk(e.getMessage(), e);
				}
				
				AngryChipmunk ex = (AngryChipmunk) e;
				
				CTraceFrame trace = new CTraceFrame();
				trace.setDebugSymbol(method.getDebugSymbol());
				trace.lineNumber = findLineNumber(ip, method.getCode().getDebugTable());
				
				ex.addTraceFrame(trace);
				
				ExceptionBlock handler = chooseExceptionHandler(ip, method.getCode().getExceptionTable());
				if(handler != null) {
					ip = handler.catchIndex;
					locals[handler.exceptionLocalIndex] = e;
				}else {
					throw ex;
				}
			}
		} else {
			locals = new Object[localCount + 1];
			stack = new OperandStack();
			locals[0] = method.getSelf();
			if (parameters != null) {
				// copy parameters
				for (int i = 0; i < parameters.length; i++) {
					locals[i + 1] = parameters[i];
				}
			}
		}

		int mark = stack.mark();

		while (true) {

			try {

				if(interrupted){
					// TODO - this is reading a volatile field on every operation
					throw new SuspendedChipmunk();
				}

				byte op = instructions[ip];

				Object rh;
				Object lh;
				Object ins;

				boolean cond1;
				boolean cond2;

				switch (op) {

				case ADD:
					stack.push(invoke(stack, "plus", 1));
					ip++;
					break;
				case SUB:
					stack.push(invoke(stack, "minus",1));
					ip++;
					break;
				case MUL:
					stack.push(invoke(stack, "mul", 1));
					ip++;
					break;
				case DIV:
					stack.push(invoke(stack, "div", 1));
					ip++;
					break;
				case FDIV:
					stack.push(invoke(stack, "fdiv", 1));
					ip++;
					break;
				case MOD:
					stack.push(invoke(stack, "mod", 1));
					ip++;
					break;
				case POW:
					stack.push(invoke(stack, "pow", 1));
					ip++;
					break;
				case INC:
					stack.push(invoke(stack, "inc", 0));
					ip++;
					break;
				case DEC:
					stack.push(invoke(stack, "dec", 0));
					ip++;
					break;
				case POS:
					stack.push(invoke(stack, "pos", 0));
					ip++;
					break;
				case NEG:
					stack.push(invoke(stack, "neg", 0));
					ip++;
					break;
				case AND:
					cond1 = ((CBoolean) invoke(stack, "truth", 0)).getValue();
					cond2 = ((CBoolean) invoke(stack, "truth", 0)).getValue();
					if (cond1 && cond2) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case OR:
					cond1 = ((CBoolean) invoke(stack, "truth", 0)).getValue();
					cond2 = ((CBoolean) invoke(stack, "truth", 0)).getValue();
					if (cond1 || cond2) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case BXOR:
					stack.push(invoke(stack, "bxor", 1));
					ip++;
					break;
				case BAND:
					stack.push(invoke(stack, "band", 1));
					ip++;
					break;
				case BOR:
					stack.push(invoke(stack, "bor", 1));
					ip++;
					break;
				case BNEG:
					stack.push(invoke(stack, "bneg", 0));
					ip++;
					break;
				case LSHIFT:
					stack.push(invoke(stack, "lshift", 1));
					ip++;
					break;
				case RSHIFT:
					stack.push(invoke(stack, "rshift", 1));
					ip++;
					break;
				case URSHIFT:
					stack.push(invoke(stack, "urshift", 1));
					ip++;
					break;
				case SETATTR:
					invoke(stack, "setAttr", 2);
					ip++;
					break;
				case GETATTR:
					stack.push(invoke(stack, "getAttr", 1));
					ip++;
					break;
				case GETAT:
					stack.push(invoke(stack, "getAt", 1));
					ip++;
					break;
				case SETAT:
					stack.push(invoke(stack, "setAt", 2));
					ip++;
					break;
				case GETLOCAL:
					stack.push(locals[fetchByte(instructions, ip + 1)]);
					ip += 2;
					break;
				case SETLOCAL:
					locals[fetchByte(instructions, ip + 1)] = stack.peek();
					ip += 2;
					break;
				case TRUTH:
					stack.push(invoke(stack, "truth", 0));
					ip++;
					break;
				case NOT:
					stack.push(new CBoolean(!((CBoolean) invoke(stack, "truth", 0)).booleanValue()));
					ip++;
					break;
				case AS:
					stack.push(invoke(stack, "as", 1));
					ip++;
					break;
				case IF:
					int target = fetchInt(instructions, ip + 1);
					ip += 5;
					// TODO - this is not suspension safe
					if (!((CBoolean) invoke(stack, "truth", 0)).booleanValue()) {
						ip = target;
					}
					break;
				case CALL:

					// Need to bump ip BEFORE calling next method.
					// Otherwise,
					// the ip will be stored in its old state and when this
					// method resumes after being suspended, it will try to
					// re-run this call.
					ip += 2;
					stack.push(invoke(stack,"call", fetchByte(instructions, ip + 1)));
					break;
				case CALLAT: {
					CString methodName = (CString) constantPool[fetchInt(instructions, ip + 2)];

					// Need to bump ip BEFORE calling next method.
					// Otherwise,
					// the ip will be stored in its old state and when this
					// method resumes after being suspended, it will try to
					// re-run this call.

					int paramCount = fetchByte(instructions, ip + 1);
					ip += 6;
					stack.push(invoke(stack, methodName.toString(), paramCount));
					break;
				}
				case GOTO:
					ip = fetchInt(instructions, ip + 1);
					break;
				case THROW:
					ins = stack.pop();
					throw new ExceptionChipmunk(ins);
				case RETURN:
					ins = stack.pop();
					assert stack.verifyMark(mark);
					return ins;
				case POP:
					stack.pop();
					ip++;
					break;
				case DUP:
					int dupIndex = fetchInt(instructions, ip + 1);
					stack.dup(dupIndex);
					ip += 5;
					break;
				case SWAP:
					int swapIndex1 = fetchInt(instructions, ip + 1);
					int swapIndex2 = fetchInt(instructions, ip + 5);
					stack.swap(swapIndex1, swapIndex2);
					ip += 9;
					break;
				case PUSH:
					int constIndex = fetchInt(instructions, ip + 1);
					Object constant = constantPool[constIndex];
					stack.push(constant);
					ip += 5;
					break;
				case EQ:
					stack.push(invoke(stack,"equals", 1));
					ip++;
					break;
				case GT:
					if (((CInteger) invoke(stack,"compare", 1)).getValue() > 0) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case LT:
					if (((CInteger) invoke(stack,"compare", 1)).getValue() < 0) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case GE:
					if (((CInteger) invoke(stack,"compare", 1)).getValue() >= 0) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case LE:
					if (((CInteger) invoke(stack,"compare", 1)).getValue() <= 0) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case IS:
					rh = stack.pop();
					lh = stack.pop();
					if (lh == rh) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case INSTANCEOF:
					stack.push(invoke(stack,"instanceOf", 1));
					ip++;
					break;
				case ITER:
					ins = stack.pop();
					stack.push(invoke(stack,"iterator", 1));
					ip++;
					break;
				case NEXT:
					ins = stack.peek();
					if (!((CIterator) ins).hasNext(this)) {
						ip = fetchInt(instructions, ip + 1);
					} else {
						stack.push(invoke(stack,"next", 1));
						ip += 5;
					}
					break;
				case RANGE:{
					// TODO
					//rh = stack.pop();
					//lh = stack.pop();
					//internalParams[3][2] = fetchByte(instructions, ip + 1) != 0;
					boolean inclusive = fetchByte(instructions, ip + 1) != 0;
					stack.push(invoke(stack,"range", 2));
					ip += 2;
					break;
				}
				case LIST:
					int elementCount = fetchInt(instructions, ip + 1);
					CList list = new CList();
					for(int i = 0; i < elementCount; i++){
						list.add(this, stack.pop());
					}
					// elements are popped in reverse order
					list.reverse();
					traceMem(8);
					stack.push(list);
					ip += 5;
					break;
				case MAP:
					elementCount = fetchInt(instructions, ip + 1);
					CMap map = new CMap();
					for(int i = 0; i < elementCount; i++){
						Object value = stack.pop();
						Object key = stack.pop();
						map.put(this, key, value);
					}
					traceMem(8);
					stack.push(map);
					ip += 5;
					break;
				case INIT:
					ins = stack.peek();
					stack.push(((Initializable) ins).getInitializer());
					ip++;
					break;
				case GETMODULE:
					ins = method.getCode().getModule().getNamespace()
							.get(constantPool[fetchInt(instructions, ip + 1)].toString());
					if(ins == null) {
						stack.push(CNull.instance());
					}else {
						stack.push(ins);
					}
					ip += 5;
					break;
				case SETMODULE:
					ins = stack.peek();
					method.getCode().getModule()
					.getNamespace()
					.set(constantPool[
							fetchInt(instructions, ip + 1)].toString(),
							ins);
					ip += 5;
					break;
				case INITMODULE:
					int importIndex = fetchInt(instructions, ip + 1);
					ip += 5;
					initModule(method.getCode(), importIndex);
					break;
				case IMPORT:
					importIndex = fetchInt(instructions, ip + 1);
					ip += 5;
					doImport(method.getCode(), importIndex);
					break;
				default:
					throw new InvalidOpcodeChipmunk(op);
				}

			} catch (RuntimeException e) {
				
				// SuspendedChipmunk must be re-thrown - don't
				// allow exception handlers to run or they will
				// block suspension
				if (e instanceof SuspendedChipmunk) {
					//this.freeze(method, ip, locals, stack);
					throw e;
				}

				// Wrap all native exceptions as Chipmunk exceptions, add trace info, and propagate
				if (!(e instanceof AngryChipmunk)) {
					e = new AngryChipmunk(e.getMessage(), e);
				}
				
				AngryChipmunk ex = (AngryChipmunk) e;
				
				CTraceFrame trace = new CTraceFrame();
				trace.setDebugSymbol(method.getDebugSymbol());
				trace.lineNumber = findLineNumber(ip, method.getCode().getDebugTable());

				ex.addTraceFrame(trace);
				
				ExceptionBlock handler = chooseExceptionHandler(ip, method.getCode().getExceptionTable());
				if(handler != null) {
					ip = handler.catchIndex;
					locals[handler.exceptionLocalIndex] = e;
				}else {
					throw e;
				}
			}
		}
	}

	private int fetchInt(byte[] instructions, int ip) {
		int b1 = instructions[ip] & 0xFF;
		int b2 = instructions[ip + 1] & 0xFF;
		int b3 = instructions[ip + 2] & 0xFF;
		int b4 = instructions[ip + 3] & 0xFF;
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	private byte fetchByte(byte[] instructions, int ip) {
		return instructions[ip];
	}

	private void initModule(CMethodCode code, int importIndex){
		CModule.Import im = code.getModule().getImports().get(importIndex);
		if(!modules.containsKey(im.getName())){
			BinaryModule newModule = loadModule(im.getName());
			if(newModule.getInitializer() != null){
				this.dispatch(newModule.getInitializer(), new Object[]{newModule});
			}
		}
	}

	private void doImport(CMethodCode code, int importIndex){
		final CModule module = code.getModule();
		final CModule.Import moduleImport = code.getModule().getImports().get(importIndex);
		final BinaryNamespace importedNamespace = modules.get(moduleImport.getName()).getNamespace();

		if(moduleImport.isImportAll()){

			//Set<String> importedNames = importedNamespace.names();

			//for(String name : importedNames){
			//	module.getNamespace().setFinal(name, importedNamespace.get(name));
			//}
		}else{

			List<String> symbols = moduleImport.getSymbols();
			List<String> aliases = moduleImport.getAliases();

			for(int i = 0; i < symbols.size(); i++){

				if(moduleImport.isAliased()){
					module.getNamespace().setFinal(aliases.get(i), importedNamespace.get(symbols.get(i)));
				}else{
					module.getNamespace().setFinal(symbols.get(i), importedNamespace.get(symbols.get(i)));
				}
			}
		}
	}

	public Object eval(String exp) {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule expModule = compiler.compileExpression(exp);

		CompiledModule compiled = jvmCompiler.compile(expModule);
		return invoke(compiled, "evaluate");
	}

	public Object invoke(Object target, String methodName){
		return invoke(target, methodName, null);
	}

	public Object invoke(Object target, String methodName, Object[] params){

		final int pCount = params != null ? params.length : 0;

		MethodType methodType = MethodType.methodType(Object.class);
		for(int i = 0; params != null && i < params.length; i++){
			methodType = methodType.appendParameterTypes(params[i] != null ? params[i].getClass() : Void.class);
		}

		try {
			MethodHandle invoker = MethodHandles.lookup()
					.bind(target, methodName, methodType);

			if(pCount > 0){
				return invoker.asSpreader(Object[].class, pCount).invokeWithArguments(params);
			}else{
				return invoker.invoke();
			}
		}catch (Throwable t){
			throw new AngryChipmunk(t);
		}

	}

	private Object invoke(OperandStack stack, String methodName, int paramCount) {

		Object target = stack.pop();

		final boolean isInterceptor = target instanceof CallInterceptor;
		// If sandboxed, force all calls to the RuntimeObject call signature
		final boolean isRuntimeObject = target instanceof RuntimeObject;
		if(securityMode == SecurityMode.SANDBOXED && !isRuntimeObject) {
			throw new AngryChipmunk(new IllegalAccessException("Cannot call to class " + target.getClass().getName() + " when running in sandboxed mode"));
		}

		// Assume that the target is not a call interceptor and that it
		// is a runtime object
		final Object[] params = new Object[paramCount + (isRuntimeObject ? 1 : 0)];
		stack.popArgs(paramCount, params);

		if(isRuntimeObject){
			params[0] = this;
		}

		final Object[] interceptedParams = isInterceptor ? Arrays.copyOfRange(params, 1, params.length) : params;

		if(isInterceptor){
			Object result = ((CallInterceptor) target).callAt(this, methodName, interceptedParams);
			// null result indicates that the interceptor did not intercept the call, so
			// continue with a plain dispatch
			if(result != null){
				return result;
			}
		}
		
		Class<?>[] paramTypes = new Class<?>[params.length];

		for (int i = 0; i < params.length; i++) {
			paramTypes[i] = params[i].getClass();
		}

		try {
			Object invokeTarget = binder.lookupMethod(target, methodName, paramTypes);
			Object retVal = dispatchInvocation(invokeTarget, target, params);
			//if(callCache[callCacheIndex] == null) {
				//Object invokeTarget = binder.lookupMethod(target, methodName, paramTypes);
			//	callCache[callCacheIndex] = invokeTarget;
				//Object retVal = dispatchInvocation(invokeTarget, target, params);
				
			return retVal != null ? retVal : CNull.instance();
			//}else {
				// TODO - need to check parameter types, call target, etc. and verify that the call is valid
				//Object invokeTarget = callCache[callCacheIndex];
				//Object retVal = dispatchInvocation(invokeTarget, target, params);
				
				//return retVal != null ? retVal : CNull.instance();
			//}
			
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new AngryChipmunk(e);
		} catch (AngryChipmunk e) {
			throw e;
		} catch (Throwable e) {
			throw new AngryChipmunk(e);
		}
	}
	
	public Object dispatchInvocation(Object callTarget, Object target, Object[] params) throws Throwable {

		if(callTarget instanceof VoidMarker) {
			switch (params.length) {
			case 0:
				((CallVoid) callTarget).call(target);
				break;
			case 1:
				((CallOneVoid) callTarget).call(target, params[0]);
				break;
			case 2:
				((CallTwoVoid) callTarget).call(target, params[0], params[1]);
				break;
			case 3:
				((CallThreeVoid) callTarget).call(target, params[0], params[1], params[2]);
				break;
			case 4:
				((CallFourVoid) callTarget).call(target, params[0], params[1], params[2], params[3]);
				break;
			case 5:
				((CallFiveVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4]);
				break;
			case 6:
				((CallSixVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5]);
				break;
			case 7:
				((CallSevenVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6]);
				break;
			case 8:
				((CallEightVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7]);
				break;
			case 9:
				((CallNineVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7], params[8]);
				break;
			case 10:
				((CallTenVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7], params[8], params[9]);
				break;
			default:
				// target is a non-direct method handle.
				MethodHandle handle = (MethodHandle) callTarget;
				handle.invoke(target, params);
			}
			return CNull.instance();
		}else {
			Object retVal = null;
			switch (params.length) {
			case 0:
				retVal = ((Call) callTarget).call(target);
				break;
			case 1:
				retVal = ((CallOne) callTarget).call(target, params[0]);
				break;
			case 2:
				retVal = ((CallTwo) callTarget).call(target, params[0], params[1]);
				break;
			case 3:
				retVal = ((CallThree) callTarget).call(target, params[0], params[1], params[2]);
				break;
			case 4:
				retVal = ((CallFour) callTarget).call(target, params[0], params[1], params[2], params[3]);
				break;
			case 5:
				retVal = ((CallFive) callTarget).call(target, params[0], params[1], params[2], params[3], params[4]);
				break;
			case 6:
				retVal = ((CallSix) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5]);
				break;
			case 7:
				retVal = ((CallSeven) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6]);
				break;
			case 8:
				retVal = ((CallEight) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7]);
				break;
			case 9:
				retVal = ((CallNine) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7], params[8]);
				break;
			case 10:
				retVal = ((CallTen) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7], params[8], params[9]);
				break;
			default:
				// target is a non-direct method handle.
				MethodHandle handle = (MethodHandle) callTarget;
				retVal = handle.invoke(target, params);
			}
			
			return retVal;
		}
	}
	
	private ExceptionBlock chooseExceptionHandler(int ip, ExceptionBlock[] eTable) {
		ExceptionBlock lastCandidate = null;
		
		if(eTable == null) {
			return null;
		}
		
		for(ExceptionBlock block : eTable) {
			if(lastCandidate == null) {
				lastCandidate = block;
			}else if(ip >= block.startIndex && ip <= block.catchIndex && block.startIndex > lastCandidate.startIndex && block.catchIndex < lastCandidate.catchIndex){
				lastCandidate = block;
			}
		}

		return lastCandidate;
	}
	
	private int findLineNumber(int ip, DebugEntry[] debugTable) {

		for(DebugEntry dbg : debugTable) {
			
			if(ip >= dbg.beginIndex && ip < dbg.endIndex) {
				return dbg.lineNumber;
			}
		}
		
		return 0;
	}

}

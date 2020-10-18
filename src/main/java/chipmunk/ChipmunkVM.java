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

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import chipmunk.runtime.ChipmunkModule;

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

	public Object eval(String exp) throws Throwable {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule expModule = compiler.compileExpression(exp);

		ChipmunkModule compiled = jvmCompiler.compile(expModule);
		return invoke(compiled, "evaluate");
	}

	public ChipmunkModule load(BinaryModule module) throws Throwable {
		return jvmCompiler.compile(module);
	}

	public Object invoke(Object target, String methodName) throws Throwable {
		return invoke(target, methodName, null);
	}

	public Object invoke(Object target, String methodName, Object[] params) throws Throwable {

		ChipmunkLinker linker = new ChipmunkLinker();
		ChipmunkLibraries libs = new ChipmunkLibraries();
		libs.registerLibrary(new NativeTypeLib());
		linker.setLibraries(libs);

		final int pCount = params != null ? params.length : 0;
		Object[] callParams = new Object[pCount + 1];
		callParams[0] = target;

		if(pCount > 0) {
			System.arraycopy(params, 0, callParams, 1, pCount);
		}

		MethodHandle invoker = linker
				.getInvocationHandle(MethodHandles.lookup(), target, Object.class, methodName, callParams);

		return invoker.invokeWithArguments(callParams);
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

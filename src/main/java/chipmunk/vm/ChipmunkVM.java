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

package chipmunk.vm;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import chipmunk.binary.*;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.ChipmunkSource;
import chipmunk.compiler.Compilation;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.runtime.NativeTypeLib;
import chipmunk.vm.invoke.*;
import chipmunk.vm.jvm.CompilationUnit;
import chipmunk.vm.jvm.JvmCompiler;
import chipmunk.runtime.ChipmunkModule;


public class ChipmunkVM {

	public enum SecurityMode {
		UNRESTRICTED, SANDBOXED
	}

	protected SecurityMode securityMode;

	protected final Binder binder;
	protected final JvmCompiler jvmCompiler;
	protected final ConcurrentHashMap<Integer, ChipmunkScript> scripts;
	protected final ForkJoinPool scriptPool;

	public ChipmunkVM() {
		this(SecurityMode.SANDBOXED);
	}

	public ChipmunkVM(SecurityMode securityMode) {

		this.securityMode = securityMode;

		binder = new Binder();
		jvmCompiler = new JvmCompiler();

		scripts = new ConcurrentHashMap<>();
		scriptPool = new ForkJoinPool();
	}

	public ChipmunkScript compileScript(Compilation compilation) throws CompileChipmunk, IOException, BinaryFormatException {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule[] modules = compiler.compile(compilation);
		return compileScript(modules);
	}

	public ChipmunkScript compileScript(InputStream is, String fileName) throws CompileChipmunk, IOException, BinaryFormatException {
		Compilation compilation = new Compilation();
		compilation.addSource(new ChipmunkSource(is, fileName));
		return compileScript(compilation);
	}

	public ChipmunkScript compileScript(BinaryModule[] modules) throws IOException, BinaryFormatException {

		BinaryModule mainModule = null;
		for (BinaryModule module : modules) {
			if (module.getNamespace().has("main")) {
				mainModule = module;
				break;
			}
		}

		if (mainModule == null) {
			throw new IllegalArgumentException("Could not find main method");
		}

		CompilationUnit unit = new CompilationUnit();
		unit.setModuleLoader(new ModuleLoader(Arrays.asList(modules)));
		unit.setEntryModule(mainModule.getName());
		unit.setEntryMethodName("main");

		ChipmunkScript script = jvmCompiler.compile(unit);
		script.setVM(this);
		script.setModuleLoader(unit.getModuleLoader());

		// TODO - assign ID & add to script pool
		return script;
	}

	public Object eval(String exp) throws Throwable {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule expModule = compiler.compileExpression(exp);

		ChipmunkModule compiled = jvmCompiler.compileModule(expModule);
		return invoke(compiled, "evaluate");
	}

	public ChipmunkModule getModule(String moduleName) throws Throwable {
		return getModule(ChipmunkScript.getCurrentScript(), moduleName);
	}

	public ChipmunkModule getModule(ChipmunkScript script, String moduleName) throws Throwable {
		ChipmunkModule module = script.modules.get(moduleName);
		if(module != null){
			return module;
		}

		module = script.getModuleLoader().load(moduleName, jvmCompiler);

		if(module == null){
			throw new ModuleLoadException(String.format("Module %s not found", moduleName));
		}

		script.modules.put(moduleName, module);
		module.initialize(this);
		return module;
	}

	public ChipmunkModule load(BinaryModule module) throws Throwable {
		return jvmCompiler.compileModule(module);
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

}

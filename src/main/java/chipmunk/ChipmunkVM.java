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
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import chipmunk.binary.*;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.invoke.*;
import chipmunk.jvm.JvmCompiler;
import chipmunk.modules.runtime.*;
import chipmunk.runtime.ChipmunkModule;


public class ChipmunkVM {

	public enum SecurityMode {
		UNRESTRICTED, SANDBOXED
	}

	protected SecurityMode securityMode;

	protected final Binder binder;
	protected final JvmCompiler jvmCompiler;
	protected final ForkJoinPool scriptPool;

	public ChipmunkVM() {
		this(SecurityMode.SANDBOXED);
	}

	public ChipmunkVM(SecurityMode securityMode) {

		this.securityMode = securityMode;

		binder = new Binder();
		jvmCompiler = new JvmCompiler();

		scriptPool = new ForkJoinPool();
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

		//ChipmunkScript script = new ChipmunkScript();
		//script.setEntryCall(mainModule.getName(), "main");

		//MemoryModuleLoader loader = new MemoryModuleLoader();
		//loader.addModule(ChipmunkModuleBuilder.buildLangModule());
		//loader.addModules(Arrays.asList(modules));
		//script.getLoaders().add(loader);

		return null;
		//return script;
	}

	private void doImport(CMethodCode code, int importIndex){
		final CModule module = code.getModule();
		final CModule.Import moduleImport = code.getModule().getImports().get(importIndex);
		//final BinaryNamespace importedNamespace = modules.get(moduleImport.getName()).getNamespace();

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
					//module.getNamespace().setFinal(aliases.get(i), importedNamespace.get(symbols.get(i)));
				}else{
					//module.getNamespace().setFinal(symbols.get(i), importedNamespace.get(symbols.get(i)));
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

}

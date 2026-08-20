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

import chipmunk.ChipmunkRuntimeException;
import chipmunk.binary.BinaryFormatException;
import chipmunk.binary.BinaryModule;
import chipmunk.compiler.*;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.runtime.MethodBinding;
import chipmunk.runtime.NativeTypeLib;
import chipmunk.vm.invoke.ChipmunkLibraries;
import chipmunk.vm.invoke.ChipmunkLinker;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;
import chipmunk.vm.jvm.*;
import jdk.dynalink.linker.GuardedInvocation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;

public class ChipmunkVM {

	protected volatile LinkingPolicy defaultLinkPolicy;
	protected volatile ChipmunkLibraries defaultLibraries;
	protected volatile JvmCompilerConfig defaultJvmCompilerConfig;

	protected final ScriptPool pool;

	public ChipmunkVM() {
		this(SecurityMode.ALLOWING);
	}

	public ChipmunkVM(SecurityMode securityMode) {

		defaultLinkPolicy = new LinkingPolicy(securityMode);
		defaultLibraries = new ChipmunkLibraries();
		defaultLibraries.registerLibrary(new NativeTypeLib());

		pool = new ScriptPool();

		defaultJvmCompilerConfig = new JvmCompilerConfig(defaultLinkPolicy, new TrapConfig());

	}

	public LinkingPolicy getDefaultLinkPolicy(){
		return defaultLinkPolicy;
	}

	public void setDefaultLinkPolicy(LinkingPolicy policy){
		defaultLinkPolicy = policy;
	}

	public void setDefaultLibraries(ChipmunkLibraries libraries){
		defaultLibraries = libraries;
	}

	public ChipmunkLibraries getDefaultLibraries(){
		return defaultLibraries;
	}

	public void start() {
		pool.start();
	}

	public void stop(){
		pool.shutdown();
	}

	public JvmCompiler createDefaultJvmCompiler(){
		return createJvmCompiler(defaultJvmCompilerConfig);
	}

	public JvmCompiler createJvmCompiler(JvmCompilerConfig config){
		if(config == null){
			config = defaultJvmCompilerConfig;
		}
		return new JvmCompiler(config);
	}

	public ChipmunkScript compileScript(Compilation compilation) throws CompileChipmunk, IOException, BinaryFormatException {
		return compileScript(createJvmCompiler(defaultJvmCompilerConfig), compilation);
	}

	public ChipmunkScript compileScript(JvmCompiler jvmCompiler, Compilation compilation) throws CompileChipmunk, IOException, BinaryFormatException {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule[] modules = compiler.compile(compilation);
		return compileScript(jvmCompiler, modules);
	}

	public ChipmunkScript compileScript(InputStream is, String fileName) throws CompileChipmunk, IOException, BinaryFormatException {
		Compilation compilation = new Compilation();
		compilation.addSource(new ChipmunkSource(is, fileName));
		return compileScript(compilation);
	}

	public ChipmunkScript compileScript(JvmCompiler jvmCompiler, BinaryModule[] modules) throws IOException, BinaryFormatException {

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

		return compileScript(jvmCompiler, unit);
	}

	public ChipmunkScript compileScript(BinaryModule[] modules) throws IOException, BinaryFormatException {
		return compileScript(createDefaultJvmCompiler(), modules);
	}

	public ChipmunkScript compileScript(CompilationUnit unit) throws IOException, BinaryFormatException {
		return compileScript(createJvmCompiler(unit.getJvmCompilerConfig()), unit);
	}

	public ChipmunkScript compileScript(JvmCompiler jvmCompiler, CompilationUnit unit) throws IOException, BinaryFormatException {
		ChipmunkScript script = jvmCompiler.compile(unit);
		//script.setVM(this);
		script.moduleLoader(unit.getModuleLoader());
		//script.setId(scriptIds.incrementAndGet());
		script.linkPolicy(defaultLinkPolicy);
		script.libs(defaultLibraries);
		//script.setJvmCompiler(jvmCompiler);

		return script;
	}

	public Object eval(String exp) throws Throwable {
		var compiler = new CVMCompiler();
		var loader = new ChipmunkClassLoader();
		var mCls = loader.define("exp", compiler.compileExpression(exp));
		var module = mCls.getConstructor().newInstance();

		return invoke(module, "evaluate");
	}

	@AllowChipmunkLinkage
	public ChipmunkModule getModule(String moduleName) throws Throwable {
		return getModule(ChipmunkScript.getCurrentScript(), moduleName);
	}

	public ChipmunkModule getModule(ChipmunkScript script, String moduleName) throws Throwable {
		ChipmunkModule module = script.modules.get(moduleName);
		if(module != null){
			return module;
		}

		module = script.moduleLoader().load(moduleName, script);

		if(module == null){
			throw new ModuleLoadException(String.format("Module %s not found", moduleName));
		}

		script.modules.put(moduleName, module);
		module.initialize(this);
		return module;
	}

	public ChipmunkModule load(BinaryModule module) {
		return load(createDefaultJvmCompiler(), module);
	}

	public ChipmunkModule load(JvmCompiler jvmCompiler, BinaryModule module) {
		JvmCompilation compilation = new JvmCompilation(module, new ModuleLoader(), defaultJvmCompilerConfig);
		return jvmCompiler.compileModule(compilation);
	}

	private Object invoke(Object target, String methodName) throws Throwable {
		return invoke(target, methodName, null);
	}

	private Object invoke(Object target, String methodName, Object[] params) throws Throwable {

		ChipmunkLinker linker = new ChipmunkLinker();
		ChipmunkLinker.setLibrariesForThread(defaultLibraries);

		final int pCount = params != null ? params.length : 0;
		Object[] callParams = new Object[pCount + 1];
		callParams[0] = target;

		if(pCount > 0) {
			System.arraycopy(params, 0, callParams, 1, pCount);
		}

		GuardedInvocation invoker = linker
				.getInvocationHandle(MethodHandles.lookup(), target, MethodType.methodType(Object.class), methodName, callParams, false);

		return invoker.getInvocation().invokeWithArguments(callParams);
	}

	public Object invoke(ChipmunkScript script, Object target, String methodName){
		return invoke(script, target, methodName, null);
	}

	public Object invoke(ChipmunkScript script, Object target, String methodName, Object[] params) {
		try {
			return pool.runInScriptPool(script, () -> {
				try {
					return invoke(target, methodName, params);
				} catch (Throwable e) {
					throw new ChipmunkRuntimeException(e);
				}
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new ChipmunkRuntimeException(e);
		}
	}

	public CompletableFuture<Object> runAsync(ChipmunkScript script) {
		return invokeAsync(script, script, "run");
	}

	public CompletableFuture<Object> invokeAsync(ChipmunkScript script, Object target, String methodName){
		return invokeAsync(script, target, methodName, null);
	}

	public CompletableFuture<Object> invokeAsync(ChipmunkScript script, Object target, String methodName, Object[] params){
		return pool.runInScriptPool(script, () -> invoke(script, target, methodName, params));
	}

	@AllowChipmunkLinkage
	public MethodBinding bind(Object target, String methodName) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		return (MethodBinding) getBinding(target, methodName).getConstructor(Object.class, String.class).newInstance(target, methodName);
	}

	@AllowChipmunkLinkage
	public MethodBinding bindArgs(MethodBinding delegate, int pos, Object[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		return (MethodBinding) getArgBinding(delegate.getClass(), pos, args.length).getConstructor(MethodBinding.class, int.class, Object[].class).newInstance(delegate, pos, args);
	}

	public Class<?> getBinding(Object target, String method){

		Objects.requireNonNull(target, "Cannot bind to null");

		var targetType = target.getClass();

		var bindingName = MethodBinding.class.getName() + "$" + targetType.getName().replace('.', '_') + "$" + method;

		var script = ChipmunkScript.getCurrentScript();
		try {
			return script.moduleLoader().getClassLoader().loadClass(bindingName);
		} catch (ClassNotFoundException e) {
			return script.getJvmCompiler().bindingFor(script.moduleLoader().getClassLoader(), bindingName, targetType, method);
		}
	}

	public Class<?> getArgBinding(Class<? extends MethodBinding> delegateType, int pos, int argCount){
		var bindingName = delegateType.getName() + "$bound$%d$%d".formatted(pos, argCount);

		var script = ChipmunkScript.getCurrentScript();
		try {
			return script.moduleLoader().getClassLoader().loadClass(bindingName);
		} catch (ClassNotFoundException e) {
			return script.getJvmCompiler().argBindingFor(script.moduleLoader().getClassLoader(), bindingName, delegateType, pos, argCount);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T proxy(Class<T> interfaceType, Object target) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		if(!interfaceType.isInterface()){
			throw new IllegalArgumentException(interfaceType.getName() + " is not an interface type");
		}

		var isSamType = isSamType(interfaceType);

		if(target instanceof MethodBinding && !isSamType){
			throw new IllegalArgumentException("MethodBinding target may only be cast to a functional interface");
		}

		var proxyName = "chipmunk.proxy." + interfaceType.getName() + "$Proxy$" + target.getClass().getName().replace('.', '$');

		var script = ChipmunkScript.getCurrentScript();
		var classloader = script.moduleLoader().getClassLoader();

		Class<T> proxyType;
		try {
			proxyType = (Class<T>) classloader.loadClass(proxyName);
		} catch (ClassNotFoundException e) {
			proxyType = script.getJvmCompiler()
					.makeProxyInterfaceImpl(script.moduleLoader().getClassLoader(), proxyName, interfaceType, isSamType);
		}

		return proxyType.getConstructor(ChipmunkScript.class, Object.class).newInstance(script, target);
	}

	protected boolean isSamType(Class<?> interfaceType){
		var nonDefaults = 0;
		for(var method : interfaceType.getDeclaredMethods()){
			if(!method.isDefault()){
				nonDefaults++;
			}
		}
		return nonDefaults == 1;
	}

}

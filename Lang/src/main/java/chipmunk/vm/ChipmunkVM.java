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

import chipmunk.binary.BinaryFormatException;
import chipmunk.binary.BinaryModule;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.ChipmunkSource;
import chipmunk.compiler.Compilation;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.runtime.MethodBinding;
import chipmunk.runtime.NativeTypeLib;
import chipmunk.vm.invoke.ChipmunkLibraries;
import chipmunk.vm.invoke.ChipmunkLinker;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;
import chipmunk.vm.jvm.CompilationUnit;
import chipmunk.vm.jvm.JvmCompilation;
import chipmunk.vm.jvm.JvmCompiler;
import chipmunk.vm.jvm.JvmCompilerConfig;
import chipmunk.vm.scheduler.Scheduler;
import jdk.dynalink.linker.GuardedInvocation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ChipmunkVM {

	protected volatile LinkingPolicy defaultLinkPolicy;
	protected volatile ChipmunkLibraries defaultLibraries;
	protected volatile JvmCompilerConfig defaultJvmCompilerConfig;

	protected final ConcurrentHashMap<Long, ChipmunkScript> runningScripts;
	protected final AtomicLong scriptIds;
	protected final ForkJoinPool scriptPool;
	protected final Scheduler scheduler;

	public ChipmunkVM() {
		this(SecurityMode.ALLOWING);
	}

	public ChipmunkVM(SecurityMode securityMode) {

		defaultLinkPolicy = new LinkingPolicy(securityMode);
		defaultLibraries = new ChipmunkLibraries();
		defaultLibraries.registerLibrary(new NativeTypeLib());

		runningScripts = new ConcurrentHashMap<>();
		scriptIds = new AtomicLong();
		// TODO - make configurable
		scriptPool = new ForkJoinPool(4,
				ForkJoinPool.defaultForkJoinWorkerThreadFactory,
				(thread, throwable) -> {throwable.printStackTrace();},
				true,
				4,
				8,
				0,
				null,
				60,
				TimeUnit.SECONDS);
		scheduler = new Scheduler();

		defaultJvmCompilerConfig = new JvmCompilerConfig();
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

	public JvmCompilerConfig getDefaultJvmCompilerConfig() {
		return defaultJvmCompilerConfig;
	}

	public void setDefaultJvmCompilerConfig(JvmCompilerConfig defaultJvmCompilerConfig) {
		this.defaultJvmCompilerConfig = defaultJvmCompilerConfig;
	}

	public void start() {
		scheduler.start();
	}

	public void stop(){
		scriptPool.shutdown();
		scheduler.shutdown();
	}

	public Scheduler getScheduler(){
		return scheduler;
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
		return compileScript(createJvmCompiler(compilation.getJvmCompilerConfig()), compilation);
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

	public ChipmunkScript compileScript(JvmCompiler jvmCompiler, InputStream is, String fileName) throws CompileChipmunk, IOException, BinaryFormatException {
		Compilation compilation = new Compilation();
		compilation.addSource(new ChipmunkSource(is, fileName));
		return compileScript(jvmCompiler, compilation);
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
		script.setVM(this);
		script.setModuleLoader(unit.getModuleLoader());
		script.setId(scriptIds.incrementAndGet());
		script.setLinkPolicy(defaultLinkPolicy);
		script.setLibs(defaultLibraries);
		script.setJvmCompiler(jvmCompiler);

		return script;
	}

	public Object eval(String exp) throws Throwable {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule expModule = compiler.compileExpression(exp);

		ChipmunkModule compiled = createDefaultJvmCompiler().compileModule(expModule);
		return invoke(compiled, "evaluate");
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

		JvmCompilerConfig compilerConfig = script.getJvmCompiler().getConfig();
		if(compilerConfig == null){
			compilerConfig = defaultJvmCompilerConfig;
		}
		module = script.getModuleLoader().load(moduleName, script.getJvmCompiler());

		if(module == null){
			throw new ModuleLoadException(String.format("Module %s not found", moduleName));
		}

		script.modules.put(moduleName, module);
		module.initialize(this);
		return module;
	}

	public boolean isModuleLoaded(String moduleName) {
		return isModuleLoaded(ChipmunkScript.getCurrentScript(), moduleName);
	}

	public boolean isModuleLoaded(ChipmunkScript script, String moduleName){
		return script.modules.containsKey(moduleName);
	}

	public ChipmunkModule load(BinaryModule module) {
		return load(createDefaultJvmCompiler(), module);
	}

	public ChipmunkModule load(JvmCompiler jvmCompiler, BinaryModule module) {
		JvmCompilation compilation = new JvmCompilation(module, new ModuleLoader());
		return jvmCompiler.compileModule(compilation);
	}

	public Object invoke(Object target, String methodName) throws Throwable {
		return invoke(target, methodName, null);
	}

	public Object invoke(Object target, String methodName, Object[] params) throws Throwable {

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

	public Object invoke(ChipmunkScript script, Object target, String methodName, Object[] params){
		runningScripts.put(script.getId(), script);

		ChipmunkScript.setCurrentScript(script);

		ChipmunkLibraries scriptLibs = script.getLibs();
		ChipmunkLinker.setLibrariesForThread(scriptLibs != null ? scriptLibs : defaultLibraries);

		scheduler.notifyInvocationBegan(script);

		try{
			return invoke(target, methodName, params);
		}catch (Throwable t){
			throw new RuntimeException(t);
		}finally{
			runningScripts.remove(script.getId());
			//ChipmunkScript.setCurrentScript(null);
			scheduler.notifyInvocationEnded(script);
		}
	}

	public Future<Object> runAsync(ChipmunkScript script) {
		return invokeAsync(script, script, "run");
	}

	public Future<Object> runAsync(ChipmunkScript script, Object[] params) {
		return invokeAsync(script, script, "run", params);
	}

	public Future<Object> invokeAsync(ChipmunkScript script, Object target, String methodName){
		return invokeAsync(script, target, methodName, null);
	}

	public Future<Object> invokeAsync(ChipmunkScript script, Object target, String methodName, Object[] params){
		scheduler.notifyQueuedForInvocation(script);
		return scriptPool.submit(() -> {
			Object value = invoke(script, target, methodName, params);
			ChipmunkScript.setCurrentScript(null);
			return value;
		});
	}

	public Future<Object> runInScriptPool(ChipmunkScript script, Callable<Object> task){
		scheduler.notifyQueuedForInvocation(script);
		return CompletableFuture.supplyAsync(() -> {
			try {
				scheduler.notifyInvocationBegan(script);
				return task.call();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			} finally {
				scheduler.notifyInvocationEnded(script);
			}
		}, scriptPool);
	}

	public Future<Void> runInScriptPool(ChipmunkScript script, Runnable task){
		scheduler.notifyQueuedForInvocation(script);
		return CompletableFuture.runAsync(() -> {
			try {
				scheduler.notifyInvocationBegan(script);
				task.run();
			} finally {
				scheduler.notifyInvocationEnded(script);
			}
		}, scriptPool);
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
			return script.getModuleLoader().getClassLoader().loadClass(bindingName);
		} catch (ClassNotFoundException e) {
			return script.getJvmCompiler().bindingFor(script.getModuleLoader().getClassLoader(), bindingName, targetType, method);
		}
	}

	public Class<?> getArgBinding(Class<? extends MethodBinding> delegateType, int pos, int argCount){
		var bindingName = delegateType.getName() + "$bound$%d$%d".formatted(pos, argCount);

		var script = ChipmunkScript.getCurrentScript();
		try {
			return script.getModuleLoader().getClassLoader().loadClass(bindingName);
		} catch (ClassNotFoundException e) {
			return script.getJvmCompiler().argBindingFor(script.getModuleLoader().getClassLoader(), bindingName, delegateType, pos, argCount);
		}
	}

}

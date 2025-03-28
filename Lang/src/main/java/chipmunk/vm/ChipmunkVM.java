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
import chipmunk.compiler.CompileChipmunk;
import chipmunk.runtime.CModule;
import chipmunk.runtime.ChipmunkModule;
import chipmunk.runtime.MethodBinding;
import chipmunk.runtime.NativeTypeLib;
import chipmunk.vm.invoke.CLinker;
import chipmunk.vm.invoke.ChipmunkLibraries;
import chipmunk.vm.invoke.ChipmunkLinker;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;
import chipmunk.vm.jvm.*;
import chipmunk.vm.scheduler.Scheduler;
import chipmunk.runtime.Fiber;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ChipmunkVM {

	protected volatile LinkingPolicy defaultLinkPolicy;
	protected volatile ChipmunkLibraries defaultLibraries;
	protected volatile JvmCompilerConfig defaultJvmCompilerConfig;
	protected volatile TrapHandler defaultTrapHandler;

	protected final ConcurrentHashMap<Long, ChipmunkScript> runningScripts;
	protected final AtomicLong scriptIds;
	protected final ExecutorService scriptExecutor;
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
		scriptExecutor = Executors.newVirtualThreadPerTaskExecutor();
		scheduler = new Scheduler();

		defaultJvmCompilerConfig = new JvmCompilerConfig(defaultLinkPolicy, new TrapConfig());

		defaultTrapHandler = new TrapHandler() {};
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

	public TrapHandler getDefaultTrapHandler(){
		return defaultTrapHandler;
	}

	public void setDefaultTrapHandler(TrapHandler handler){
		defaultTrapHandler = handler;
	}

	public void start() {
		scheduler.start();
	}

	public void stop(){
		scriptExecutor.shutdown();
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

	public ChipmunkScript compileScript(InputStream is, String fileName) throws CompileChipmunk, IOException, BinaryFormatException {
		var compiler = new ChipmunkCompiler();
		var modules = compiler.compile(is, fileName);
		return compileScript(modules);
	}

	public ChipmunkScript compileScript(BinaryModule[] modules) throws IOException, BinaryFormatException {

		BinaryModule mainModule = null;
		for (var module : modules) {
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

		return compileScript(unit);
	}

	public ChipmunkScript compileScript(CompilationUnit unit) {

		ChipmunkScript script = new ChipmunkScript(this);
		script.setModuleLoader(unit.getModuleLoader());
		script.setId(scriptIds.incrementAndGet());
		script.setLinkPolicy(defaultLinkPolicy);
		script.setLibs(defaultLibraries);

		return script;
	}

	public Object eval(String exp) {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		// TODO - load module
		//CModule expModule = compiler.compileExpression(exp);
		//return invoke(expModule, "evaluate");
		return null;
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

		module = script.getModuleLoader().load(moduleName);

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

	/*public static Object hostInvoke(Fiber fiber, Object target, String name, Object... args){
		var node = new CallAtNode(0, new Value(target), name, Arrays.stream(args).map(Value::new).toArray(Node[]::new));
		return node.execute(fiber);
	}*/

	public Object invoke(Object target, String methodName, Object... params) {
		// TODO - set up fiber properly
		/*var fiber = new Fiber(this);
		var node = new CallAtNode(0, new Value(target), methodName, Arrays.stream(params).map(Value::new).toArray(Node[]::new));
		return node.execute(fiber);*/
		return null; // TODO
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

	public CLinker getDefaultLinker(){
		var libs = new ChipmunkLibraries();
		libs.registerLibrary(new NativeTypeLib());
		return new CLinker(null, libs);
	}

	public CompletableFuture<Object> runAsync(ChipmunkScript script) {
		return invokeAsync(script, script, "run");
	}

	public CompletableFuture<Object> runAsync(ChipmunkScript script, Object[] params) {
		return invokeAsync(script, script, "run", params);
	}

	public CompletableFuture<Object> invokeAsync(ChipmunkScript script, Object target, String methodName){
		return invokeAsync(script, target, methodName, null);
	}

	public CompletableFuture<Object> invokeAsync(ChipmunkScript script, Object target, String methodName, Object[] params){
		scheduler.notifyQueuedForInvocation(script);
		return CompletableFuture.supplyAsync(() -> {
			Object value = invoke(script, target, methodName, params);
			ChipmunkScript.setCurrentScript(null);
			return value;
		}, scriptExecutor);
	}

	public CompletableFuture<Object> runInScriptPool(ChipmunkScript script, Callable<Object> task){
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
		}, scriptExecutor);
	}

	public CompletableFuture<Void> runInScriptPool(ChipmunkScript script, Runnable task){
		scheduler.notifyQueuedForInvocation(script);
		return CompletableFuture.runAsync(() -> {
			try {
				scheduler.notifyInvocationBegan(script);
				task.run();
			} finally {
				scheduler.notifyInvocationEnded(script);
			}
		}, scriptExecutor);
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
		var classloader = script.getModuleLoader().getClassLoader();

		Class<T> proxyType;
		try {
			proxyType = (Class<T>) classloader.loadClass(proxyName);
		} catch (ClassNotFoundException e) {
			proxyType = script.getJvmCompiler()
					.makeProxyInterfaceImpl(script.getModuleLoader().getClassLoader(), proxyName, interfaceType, isSamType);
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

	private static void handleTrap(Consumer<TrapHandler> h){
		var script = ChipmunkScript.getCurrentScript();
		if(script != null){
			//var handler = script.getTrapHandler();
			//if(handler != null){
			//	h.accept(handler);
			//}
		}
	}

	public static void trap(Object payload){
		handleTrap(handler -> handler.runtimeTrap(payload));
	}

	public static void backJump(TrapSite site){
		handleTrap(handler -> handler.backJump(site));
	}

	public static void trapObjectAlloc(TrapSite site, Class<?> objectType){
		handleTrap(hander -> hander.objectAlloc(site, objectType));
	}

	public static void trapArrayAlloc(TrapSite site, Class<?> arrayType, int dimensions, int capacity){
		handleTrap(handler -> handler.arrayAlloc(site, arrayType, dimensions, capacity));
	}

	public static void trapMethodCall(TrapSite site, MethodIdentifier method){
		handleTrap(handler -> handler.methodCall(site, method));
	}

	public static void trapObjectInit(TrapSite site, Object object){
		handleTrap(handler -> handler.postObjectInit(site, object));
	}

}

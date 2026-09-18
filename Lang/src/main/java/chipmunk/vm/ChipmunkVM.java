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

import chipmunk.binary.BinaryModule;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.ChipmunkSource;
import chipmunk.compiler.Compilation;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.modules.lang.LangModule;
import chipmunk.vm.hazel.EntryPoint;
import chipmunk.vm.hazel.Limits;
import chipmunk.vm.invoke.security.LinkingPolicy;
import chipmunk.vm.invoke.security.SecurityMode;
import chipmunk.vm.jvm.*;
import chipmunk.vm.scheduler.Scheduler;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ChipmunkVM {

	protected volatile LinkingPolicy defaultLinkPolicy;

	protected final ConcurrentHashMap<Long, ChipmunkScript> runningScripts;
	protected final AtomicLong scriptIds;
	protected final ExecutorService scriptExecutor;
	protected final Scheduler scheduler;
	protected final ModuleLoader rootLoader;

	protected Limits defaultLimits;

	public ChipmunkVM() {
		this(SecurityMode.DENYING);
	}

	public ChipmunkVM(SecurityMode securityMode) {

		defaultLinkPolicy = new LinkingPolicy(securityMode);

		runningScripts = new ConcurrentHashMap<>();
		scriptIds = new AtomicLong();
		scriptExecutor = Executors.newVirtualThreadPerTaskExecutor();
		scheduler = new Scheduler();

		rootLoader = new ModuleLoader();
		rootLoader.registerNativeFactory(LangModule.MODULE_NAME, LangModule::new);

		defaultLimits = new Limits();
	}

	public ModuleLoader rootLoader(){
		return rootLoader;
	}

	public LinkingPolicy getDefaultLinkPolicy(){
		return defaultLinkPolicy;
	}

	public void setDefaultLinkPolicy(LinkingPolicy policy){
		defaultLinkPolicy = policy;
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

	public ChipmunkScript compileScript(InputStream is, String fileName) throws CompileChipmunk {
		Compilation compilation = new Compilation();
		compilation.addSource(new ChipmunkSource(is, fileName));
		return compileScript(compilation);
	}

	public ChipmunkScript compileScript(Compilation compilation) {
		var compiler = new ChipmunkCompiler();
		var modules = compiler.compile(compilation);
		return compileScript(modules);
	}

	public ChipmunkScript compileScript(BinaryModule... modules){
		return compileScript(EntryPoint.DEFAULT, modules);
	}

	public ChipmunkScript compileScript(EntryPoint entryPoint, BinaryModule... modules) {

		var script = new ChipmunkScript(this, scriptIds.incrementAndGet(), new ModuleLoader(rootLoader, Arrays.asList(modules)));
		script.getHazelVM().limits().copyFrom(defaultLimits);
		script.getHazelVM().entryPoint(entryPoint);

		return script;
	}

	public Object eval(String exp) {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule expModule = compiler.compileExpression(exp);

		var script = compileScript(new EntryPoint("exp", "evaluate"), expModule);
		var result = script.run();
		while(result.isEmpty()){
			result = script.run();
		}

		return result.get();
	}

	/*public Object invoke(Object target, String methodName, Object[] params) throws Throwable {

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
	}*/

	/*public Object invoke(ChipmunkScript script, Object target, String methodName){
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
	}*/

	/*public CompletableFuture<Object> runAsync(ChipmunkScript script) {
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
	}*/

	/*public CompletableFuture<Object> runInScriptPool(ChipmunkScript script, Callable<Object> task){
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
	}*/

}

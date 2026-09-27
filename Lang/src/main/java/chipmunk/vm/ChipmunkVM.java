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
import chipmunk.vm.hazel.HazelVM;
import chipmunk.vm.hazel.Limits;
import chipmunk.vm.invoke.LinkingPolicy;
import chipmunk.vm.invoke.SecurityMode;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ChipmunkVM {

	protected volatile LinkingPolicy defaultLinkPolicy;

	protected final ConcurrentHashMap<Long, ChipmunkScript> scripts;
	protected final AtomicLong scriptIds;
	protected final ExecutorService scriptExecutor;
	protected final Scheduler scheduler;
	protected final ModuleLoader rootLoader;

	protected Limits defaultLimits;

	public ChipmunkVM() {
		this(SecurityMode.DENYING, (int) (Runtime.getRuntime().availableProcessors() * 0.5) + 1);
	}

	public ChipmunkVM(SecurityMode securityMode, int threadCount){
		this(securityMode, threadCount, Executors.newFixedThreadPool(threadCount,
				(task) -> Thread.ofPlatform()
						.name("ChipmunkRunner")
						.unstarted(task)));
	}

	public ChipmunkVM(SecurityMode securityMode, int threadCount, ExecutorService threads) {

		defaultLinkPolicy = new LinkingPolicy(securityMode);

		scripts = new ConcurrentHashMap<>();
		scriptIds = new AtomicLong();
		scriptExecutor = threads;
		scheduler = new Scheduler(threadCount, scriptExecutor, (script) -> 0);

		rootLoader = new ModuleLoader();
		rootLoader.registerNativeFactory(LangModule.MODULE_NAME, LangModule::new);

		defaultLimits = new Limits();

		scheduler.start();
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

	public void stop(){
		scriptExecutor.shutdown();
		scheduler.shutdown();
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

		scripts.put(script.getId(), script);

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

		return result.value();
	}

	public CompletableFuture<Object> run(ChipmunkScript script){
		return scheduler.enqueue(script);
	}

	public boolean exitScript(ChipmunkScript script, boolean force) {
		if(force || script.getHazelVM().state() == HazelVM.State.EXITED){
			script.setStatus(ChipmunkScript.Status.EXITED);
			if(script.exitHandler() != null){
				script.exitHandler().accept(script);
			}
			scripts.remove(script.getId());
			return true;
		}
		return false;
	}

}

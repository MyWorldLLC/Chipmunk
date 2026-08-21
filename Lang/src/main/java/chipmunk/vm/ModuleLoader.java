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
import chipmunk.compiler.CompileChipmunk;
import chipmunk.compiler.ModuleClasses;
import chipmunk.modules.lang.LangModule;
import chipmunk.runtime.CompiledModule;
import chipmunk.vm.jvm.ChipmunkClassLoader;
import chipmunk.runtime.ChipmunkModule;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModuleLoader {

	protected final ModuleLoader delegate;
	protected final List<ModuleLocator> locators;
	protected final Map<String, CompiledModule> loadedModules;
	protected final Map<String, NativeModuleFactory> nativeFactories;
	protected final ChipmunkClassLoader classLoader;

	public ModuleLoader(){
		this(null);
	}

	public ModuleLoader(ModuleLoader delegate){
		this.delegate = delegate;
		locators = new CopyOnWriteArrayList<>();
		loadedModules = new ConcurrentHashMap<>();
		nativeFactories = new ConcurrentHashMap<>();
		classLoader = new ChipmunkClassLoader();

		registerNativeFactory(LangModule.MODULE_NAME, LangModule::new);
	}

	public ModuleLoader delegate(){
		return delegate;
	}

	public ChipmunkClassLoader classLoader(){
		return classLoader;
	}

	public void addLocator(ModuleLocator locator){
		locators.add(locator);
	}

	public void removeLocator(ModuleLocator locator){
		Iterator<ModuleLocator> it = locators.iterator();
		while(it.hasNext()){
			ModuleLocator l = it.next();
			if(l == locator){
				it.remove();
				return;
			}
		}
	}

	public List<ModuleLocator> getLocators(){
		return locators;
	}

	public InputStream locate(String moduleName) throws IOException {
		for(ModuleLocator locator : locators){
			InputStream is = locator.locate(moduleName);
			if(is != null){
				return is;
			}
		}

		return null;
	}

	private boolean loadModule(ChipmunkScript script, String moduleName) throws IOException, CompileChipmunk {

		if(loadedModules.containsKey(moduleName)){
			throw new IllegalArgumentException("Module already loaded: " + moduleName);
		}

		InputStream is = locate(moduleName);
		if(is == null){
			if(delegate != null){
				return delegate.loadModule(script, moduleName);
			}
			return false;
		}

		var compiler = script.vm().compilerFor(script);
		var modules = compiler.compile(is, moduleName);
		modules.forEach(this::define);

		loadedModules.put(moduleName, new CompiledModule(moduleName, "TODO", 0));

		return true;
	}

	public ChipmunkModule loadNative(String moduleName){
		NativeModuleFactory nativeFactory = nativeFactories.get(moduleName);
		if(nativeFactory == null){
			if(delegate != null){
				return delegate.loadNative(moduleName);
			}
			return null;
		}
		return nativeFactory.createModule();
	}

	public ChipmunkModule load(String moduleName, ChipmunkScript script) throws CompileChipmunk {
		if(loadedModules.containsKey(moduleName)){
			return instance(moduleName);
        }

        try {
            var loaded = loadModule(script, moduleName);
			if(!loaded) {
				return loadNative(moduleName);
			}

			return instance(moduleName);
        } catch (IOException e) {
            throw new ChipmunkRuntimeException(e);
        }

	}

	protected ChipmunkModule instance(String moduleName){
		try {
			return (ChipmunkModule) classLoader.loadClass(loadedModules.get(moduleName).className()).getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
		         NoSuchMethodException | ClassNotFoundException e) {
			throw new ChipmunkRuntimeException(e);
		}
	}

	public void define(ModuleClasses classes){
		var module = classes.compiledModule();
		if(!loadedModules.containsKey(module.name())){
			loadedModules.put(module.name(), module);
			classes.classes().forEach(classLoader::define);
		}
	}

	public void removeDefined(String name){
		loadedModules.remove(name);
	}

	public void registerNativeFactory(String name, NativeModuleFactory factory){
		nativeFactories.put(name, factory);
	}

	public void unregisterNativeFactory(String name){
		nativeFactories.remove(name);
	}

	public Map<String, NativeModuleFactory> getNativeFactories(){
		return nativeFactories;
	}

}

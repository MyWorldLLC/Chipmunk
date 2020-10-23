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

import chipmunk.binary.BinaryFormatException;
import chipmunk.binary.BinaryModule;
import chipmunk.binary.BinaryReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModuleLoader {

	protected final List<ModuleLocator> locators;
	protected final Map<String, BinaryModule> loadedModules;

	public ModuleLoader(){
		locators = new CopyOnWriteArrayList<>();
		loadedModules = new ConcurrentHashMap<>();
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

	BinaryModule loadModule(String moduleName) throws IOException, BinaryFormatException {

		if(loadedModules.containsKey(moduleName)){
			return loadedModules.get(moduleName);
		}

		InputStream is = locate(moduleName);
		if(is == null){
			return null;
		}

		BinaryReader reader = new BinaryReader();
		BinaryModule module = reader.readModule(is);

		loadedModules.put(moduleName, module);

		return module;
	}

	public Map<String, BinaryModule> getLoadedModules(){
		return loadedModules;
	}

	public void addToLoaded(BinaryModule module){
		loadedModules.putIfAbsent(module.getName(), module);
	}

	public void addToLoaded(Collection<BinaryModule> modules){
		for(BinaryModule module : modules){
			loadedModules.putIfAbsent(module.getName(), module);
		}
	}

	public void removeFromLoaded(String moduleName){
		loadedModules.remove(moduleName);
	}

	public void removeFromLoaded(BinaryModule module){
		loadedModules.remove(module.getName());
	}

}

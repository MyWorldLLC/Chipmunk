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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import chipmunk.modules.runtime.CModule;

public class MemoryModuleLoader implements ModuleLoader {

	private final Map<String, CModule> modules;
	
	public MemoryModuleLoader(){
		modules = new HashMap<String, CModule>();
	}

	public MemoryModuleLoader(Collection<CModule> modules){
		this();
		addModules(modules);
	}
	
	@Override
	public CModule loadModule(String moduleName) {
		// TODO - module duplication
		return modules.get(moduleName);
	}
	
	public void addModule(CModule module){
		modules.put(module.getName(), module);
	}
	
	public void addModules(Collection<CModule> modules){
		for(CModule module : modules){
			this.modules.put(module.getName(), module);
		}
	}
	
	public boolean hasModule(String moduleName){
		return modules.containsKey(moduleName);
	}
	
	public void removeModule(String moduleName){
		modules.remove(moduleName);
	}
}

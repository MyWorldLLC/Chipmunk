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

import java.util.*;

import chipmunk.ChipmunkVM.CallFrame;
import chipmunk.modules.runtime.CModule;

public class ChipmunkScript {

	protected List<ModuleLoader> loaders;
	protected Map<String, CModule> modules;

	protected final List<Object> tags;

	protected Deque<CallFrame> frozenCallStack;
	private boolean initialized;

	protected String entryModule;
	protected String entryMethod;
	protected Object[] entryArgs;
	
	public ChipmunkScript(){
		this(128);
	}
	
	public ChipmunkScript(int initialStackDepth){
		loaders = new ArrayList<>();
		modules = new HashMap<>();
		tags = new ArrayList<>();
		frozenCallStack = new ArrayDeque<>();
		initialized = false;
	}
	
	public boolean isFrozen(){
		return !frozenCallStack.isEmpty();
	}
	public boolean isInitialized(){ return initialized; }

	protected void markInitialized(){
		initialized = true;
	}

	public void setEntryCall(String module, String method, Object... args){
		entryModule = module;
		entryMethod = method;
		entryArgs = args;
	}
	
	public void setEntryCall(String module, String method){
		setEntryCall(module, method, new Object[]{});
	}

	public String getEntryModule(){
		return entryModule;
	}

	public String getEntryMethod(){
		return entryMethod;
	}

	public Map<String, CModule> getModules(){
		return modules;
	}
	public void setModule(CModule module){
		modules.put(module.getName(), module);
	}

	public List<ModuleLoader> getLoaders(){
		return loaders;
	}

	public void setLoaders(List<ModuleLoader> loaders){
		this.loaders = loaders;
	}

	public synchronized void tag(Object tag){
		tags.add(tag);
	}

	public synchronized void removeTag(Object tag){
		tags.remove(tag);
	}
	public synchronized <T> T getTag(Class<?> tagType){
		for(Object o : tags){
			if(tagType.isInstance(o)){
				return (T) o;
			}
		}
		return null;
	}
	public synchronized List<Object> getTagsUnmodifiable(){
		return Collections.unmodifiableList(tags);
	}
}

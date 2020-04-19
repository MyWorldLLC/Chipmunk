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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalMethodCache {
	
	protected final Map<Class<?>, Map<String, List<MethodHandle>>> cache;
	
	public InternalMethodCache() {
		cache = new HashMap<>();
	}
	
	public void cache(Class<?> type, String name, MethodHandle handle) {
		Map<String, List<MethodHandle>> typeEntry = cache.get(type);
		
		if(typeEntry == null) {
			// class not known
			List<MethodHandle> handles = new ArrayList<>(1);
			handles.add(handle);
			
			typeEntry = new HashMap<>();
			typeEntry.put(name, handles);
			
			cache.put(type, typeEntry);
		}else {
			// class is known
			List<MethodHandle> handles = typeEntry.get(name);
			
			if(handles == null) {
				// method with this name is not known
				handles = new ArrayList<>(1);
				handles.add(handle);
				typeEntry.put(name, handles);
			}else {
				// method is known - only insert if there is not already a matching method handle
				if(!handles.contains(handle)) {
					handles.add(handle);
				}
			}
		}
	}
	
	public MethodHandle get(Class<?> type, String name, MethodType signature) {
		Map<String, List<MethodHandle>> typeEntry = cache.get(type);
		if(typeEntry == null) {
			return null;
		}
		
		List<MethodHandle> handles = typeEntry.get(name);
		if(handles == null) {
			return null;
		}
		
		for(int i = 0; i < handles.size(); i++) {
			MethodHandle handle = handles.get(i);
			if(handle.type().equals(signature)) {
				return handle;
			}
		}
		
		return null;
	}
	
	

}

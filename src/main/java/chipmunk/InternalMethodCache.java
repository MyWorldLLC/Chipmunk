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

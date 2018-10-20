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

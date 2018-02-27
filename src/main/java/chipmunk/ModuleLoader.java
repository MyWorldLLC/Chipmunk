package chipmunk;

import chipmunk.modules.reflectiveruntime.CModule;

public interface ModuleLoader {

	public CModule loadModule(String moduleName);
	
}

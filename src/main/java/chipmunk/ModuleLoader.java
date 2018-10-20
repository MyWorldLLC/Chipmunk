package chipmunk;

import chipmunk.modules.runtime.CModule;

public interface ModuleLoader {

	public CModule loadModule(String moduleName) throws Exception;
	
}

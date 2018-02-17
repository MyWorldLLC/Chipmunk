package chipmunk.modules.reflectiveruntime;

import java.util.Collections;
import java.util.List;

import chipmunk.Namespace;

public class CModule {
	
	private final List<Object> constants;
	private final Namespace namespace;
	
	public CModule(List<Object> constantPool){
		constants = constantPool;
		namespace = new Namespace();
	}
	
	public List<Object> getConstantsUnmodifiable(){
		return Collections.unmodifiableList(constants);
	}
	
	public Namespace getNamespace(){
		return namespace;
	}
}

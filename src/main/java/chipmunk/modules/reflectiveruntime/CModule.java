package chipmunk.modules.reflectiveruntime;

import java.util.Collections;
import java.util.List;

import chipmunk.Namespace;

public class CModule {
	
	private final List<Object> constants;
	private final Namespace namespace;
	private final String name;
	
	public CModule(String name, List<Object> constantPool){
		this.name = name;
		constants = constantPool;
		namespace = new Namespace();
	}
	
	public List<Object> getConstantsUnmodifiable(){
		return Collections.unmodifiableList(constants);
	}
	
	public Namespace getNamespace(){
		return namespace;
	}
	
	public String getName(){
		return name;
	}
}

package chipmunk.modules.reflectiveruntime;

import chipmunk.Namespace;

public class CObject implements RuntimeObject{
	
	private final CClass cClass;
	
	private final Namespace methods;
	private final Namespace attributes;
	
	public CObject(CClass cls, Namespace attributes, Namespace methods){
		cClass = cls;
		
		this.attributes = attributes;
		this.methods = methods;
	}
	
	public Namespace getAttributes(){
		return attributes;
	}
	
	public Namespace getMethods(){
		return methods;
	}

}

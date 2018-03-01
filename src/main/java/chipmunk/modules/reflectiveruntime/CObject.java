package chipmunk.modules.reflectiveruntime;

import chipmunk.Namespace;

public class CObject implements RuntimeObject, Initializable {
	
	private final CClass cClass;
	
	private final Namespace methods;
	private final Namespace attributes;
	
	protected CMethod initializer;
	
	public CObject(CClass cls, Namespace attributes, Namespace methods){
		cClass = cls;
		
		this.attributes = attributes;
		this.methods = methods;
	}
	
	public CClass getCClass(){
		return cClass;
	}
	
	public Namespace getAttributes(){
		return attributes;
	}
	
	public Namespace getMethods(){
		return methods;
	}

	public CMethod getInitializer(){
		return initializer;
	}
	
	public boolean hasInitializer(){
		return initializer != null;
	}
	
	public void setInitializer(CMethod initializer){
		this.initializer = initializer;
	}

}

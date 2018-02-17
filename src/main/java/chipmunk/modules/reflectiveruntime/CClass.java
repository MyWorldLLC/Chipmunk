package chipmunk.modules.reflectiveruntime;

import chipmunk.Namespace;

public class CClass implements RuntimeObject{

	private final Namespace sharedAttributes;
	private final Namespace sharedMethods;
	
	private final Namespace protoNamespace;
	private final Namespace protoMethods;
	
	public CClass(){
		sharedAttributes = new Namespace();
		sharedMethods = new Namespace();
		
		protoNamespace = new Namespace();
		protoMethods = new Namespace();
	}
	
	public Namespace getAttributes(){
		return sharedAttributes;
	}
	
	public Namespace getMethods(){
		return sharedMethods;
	}
}

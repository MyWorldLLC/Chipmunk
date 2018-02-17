package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;
import chipmunk.Namespace;

public class CClass implements RuntimeObject{

	private final Namespace sharedAttributes;
	private final Namespace sharedMethods;
	
	private final Namespace protoNamespace;
	private final Namespace protoMethods;
	
	private final String name;
	
	public CClass(String name){
		sharedAttributes = new Namespace();
		sharedMethods = new Namespace();
		
		protoNamespace = new Namespace();
		protoMethods = new Namespace();
		
		this.name = name;
	}
	
	public Namespace getAttributes(){
		return sharedAttributes;
	}
	
	public Namespace getMethods(){
		return sharedMethods;
	}
	
	public Object newInstance(ChipmunkVM vm, Integer paramCount){
		
		// TODO - memory tracing
		CObject obj = new CObject(this, protoNamespace.duplicate(), protoMethods.duplicate());
		
		if(protoMethods.has(name)){
			vm.dispatch((CMethod)obj.getMethods().get(name), paramCount);
		}
		
		return obj;
	}
}

package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;
import chipmunk.Namespace;

public class CClass implements RuntimeObject{

	private final Namespace sharedAttributes;
	private final Namespace sharedMethods;
	
	private final Namespace instanceAttributes;
	private final Namespace instanceMethods;
	
	private final String name;
	private final CModule module;
	
	private CMethod sharedInitializer;
	private CMethod instanceInitializer;
	
	public CClass(String name, CModule module){
		sharedAttributes = new Namespace();
		sharedMethods = new Namespace();
		
		instanceAttributes = new Namespace();
		instanceMethods = new Namespace();
		
		this.name = name;
		this.module = module;
	}
	
	public Namespace getAttributes(){
		return sharedAttributes;
	}
	
	public Namespace getMethods(){
		return sharedMethods;
	}
	
	public Namespace getInstanceAttributes(){
		return instanceAttributes;
	}
	
	public Namespace getInstanceMethods(){
		return instanceMethods;
	}
	
	public String getName(){
		return name;
	}
	
	public CModule getModule(){
		return module;
	}
	
	public Object newInstance(ChipmunkVM vm, Integer paramCount){
		
		// TODO - memory tracing
		CObject obj = new CObject(this, instanceAttributes.duplicate(), instanceMethods.duplicate());
		
		if(instanceMethods.has(name)){
			vm.dispatch((CMethod)obj.getMethods().get(name), paramCount);
		}
		
		return obj;
	}

	public CMethod getSharedInitializer() {
		return sharedInitializer;
	}

	public void setSharedInitializer(CMethod sharedInitializer) {
		this.sharedInitializer = sharedInitializer;
	}

	public CMethod getInstanceInitializer() {
		return instanceInitializer;
	}

	public void setInstanceInitializer(CMethod instanceInitializer) {
		this.instanceInitializer = instanceInitializer;
	}
	
	
}

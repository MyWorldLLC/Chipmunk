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
	
	public Object call(ChipmunkVM vm, Byte paramCount){
		
		// TODO - memory tracing
		CObject obj = new CObject(this, instanceAttributes.duplicate(), instanceMethods.duplicate());
		obj.setInitializer(instanceInitializer.duplicate(vm));
		obj.getInitializer().bind(obj);
		
		// Invoke constructor (compiler ensures that all classes have exactly one constructor).
		// This is suspension/exception-safe because (a) any exceptions will seamlessly propagate
		// out of this method and back into the VM, and (b) any suspension will freeze the running
		// constructor, skip this, and continue freezing the previous call stack. Since this method
		// just returns what the constructor returns (the newly created object), when the call stack
		// is resumed this method will not be invoked, the constructor will resume where it left off,
		// and the VM will have pushed the newly created object onto the stack when the constructor
		// returns.
		return vm.dispatch((CMethod)obj.getAttributes().get(name), paramCount.intValue());
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
	
	public Object setAttr(ChipmunkVM vm, String name, Object value){
		vm.traceMem(8);
		sharedAttributes.set(name, value);
		return value;
	}
	
	public Object getAttr(ChipmunkVM vm, String name){
		vm.traceMem(8);
		return sharedAttributes.get(name);
	}
	
}

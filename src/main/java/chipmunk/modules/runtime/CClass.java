package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;
import chipmunk.Namespace;
import chipmunk.RuntimeObject;

public class CClass implements RuntimeObject, Initializable, CallInterceptor, CCallable {

	private final Namespace sharedAttributes;
	
	private final Namespace instanceAttributes;
	
	private final String name;
	private final CModule module;
	
	private CMethod sharedInitializer;
	private CMethod instanceInitializer;
	
	public CClass(String name, CModule module){
		sharedAttributes = new Namespace();
		
		instanceAttributes = new Namespace();
		
		this.name = name;
		this.module = module;
	}
	
	public Namespace getAttributes(){
		return sharedAttributes;
	}
	
	public Namespace getInstanceAttributes(){
		return instanceAttributes;
	}

	public CString getName(ChipmunkVM vm){
		vm.traceReference();
		return new CString(name);
	}

	public CString getFullName(ChipmunkVM vm){
		vm.traceReference();
		return new CString(getFullName());
	}

	public String getFullName(){
		return String.format("%s.%s", module.getName(), getName());
	}

	public String getName(){
		return name;
	}
	
	public CModule getModule(){
		return module;
	}
	
	public Object call(ChipmunkVM vm, Object[] params){
		
		// TODO - memory tracing
		vm.traceReference();
		CObject obj = (CObject) instantiate();
		
		// Invoke constructor (compiler ensures that all classes have exactly one constructor).
		// This is suspension/exception-safe because (a) any exceptions will seamlessly propagate
		// out of this method and back into the VM, and (b) any suspension will freeze the running
		// constructor, skip this, and continue freezing the previous call stack. Since this method
		// just returns what the constructor returns (the newly created object), when the call stack
		// is resumed this method will not be invoked, the constructor will resume where it left off,
		// and the VM will have pushed the newly created object onto the stack when the constructor
		// returns.
		return vm.dispatch((CMethod)obj.getAttributes().get(name), params);
	}

	public Object instantiate(){
		CObject obj = new CObject(this, instanceAttributes.duplicate());

		CMethod initializer = instanceInitializer.duplicate();
		initializer.bind(obj);

		obj.setInitializer(initializer);
		obj.getAttributes().set("class", this);
		System.out.println("Instantiating class " + name + ": " + obj);
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
	
	public Object setAttr(ChipmunkVM vm, String name, Object value){
		vm.traceMem(8);
		sharedAttributes.set(name, value);
		return value;
	}
	
	public Object getAttr(ChipmunkVM vm, String name){
		vm.traceMem(8);
		return sharedAttributes.get(name);
	}
	
	public CBoolean instanceOf(ChipmunkVM vm, CClass clazz) {
		if(clazz.getName().equals("Class") && clazz.getModule().getName().equals("chipmunk.lang")) {
			vm.traceBoolean();
			return new CBoolean(true);
		}
		vm.traceBoolean();
		return new CBoolean(false);
	}

	@Override
	public CMethod getInitializer() {
		return sharedInitializer;
	}

	@Override
	public Object callAt(ChipmunkVM vm, String methodName, Object[] params) {
		CCallable callable = (CCallable) sharedAttributes.get(methodName);
		if(callable != null){
			return callable.call(vm, params);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "class " + module.getName() + "." + (!name.equals("") ? name : "<anonymous>");
	}
	
}

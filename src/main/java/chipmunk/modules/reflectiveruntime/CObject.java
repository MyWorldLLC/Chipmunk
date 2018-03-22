package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;
import chipmunk.Namespace;

public class CObject implements RuntimeObject, Initializable, CallInterceptor {
	
	private final CClass cClass;
	
	private final Namespace attributes;
	
	protected CMethod initializer;
	
	public CObject(CClass cls, Namespace attributes){
		cClass = cls;
		
		this.attributes = attributes;
		
		for(String name : attributes.names()){
			Object attr = attributes.get(name);
			
			if(attr instanceof CMethod){
				((CMethod) attr).bind(this);
			}
		}
	}
	
	public CClass getCClass(){
		return cClass;
	}
	
	public CClass getClass(ChipmunkVM vm){
		vm.traceMem(8);
		return cClass;
	}
	
	public Namespace getAttributes(){
		return attributes;
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
	
	public Object setAttr(ChipmunkVM vm, String name, Object value){
		vm.traceMem(8);
		attributes.set(name, value);
		return value;
	}
	
	public Object getAttr(ChipmunkVM vm, String name){
		vm.traceMem(8);
		return attributes.get(name);
	}
	
	@Override
	public Object callAt(ChipmunkVM vm, String methodName, int paramCount) {
		CMethod method = (CMethod) attributes.get(methodName);
		if(method != null){
			return vm.dispatch(method, paramCount);
		}
		return null;
	}

}

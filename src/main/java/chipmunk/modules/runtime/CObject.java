package chipmunk.modules.runtime;

import java.util.List;

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
		vm.traceReference();
		attributes.set(name, value);
		return value;
	}
	
	public Object getAttr(ChipmunkVM vm, String name){
		return attributes.get(name);
	}
	
	public CBoolean instanceOf(ChipmunkVM vm, CClass clazz) {
		if(clazz == cClass) {
			vm.traceBoolean();
			return new CBoolean(true);
		}else {
			List<Object> traitAttributes = attributes.traitAttributes();
			for(int i = 0; i < traitAttributes.size(); i++) {
				Object trait = traitAttributes.get(i);
				if(trait instanceof CObject) {
					CBoolean isInstance = ((CObject) trait).instanceOf(vm, clazz);
					if(isInstance.booleanValue()) {
						vm.traceBoolean();
						return isInstance;
					}
				}else if(trait instanceof CClass) {
					CBoolean isInstance = ((CClass) trait).instanceOf(vm, clazz);
					if(isInstance.booleanValue()) {
						vm.traceBoolean();
						return isInstance;
					}
				}
			}
		}
		vm.traceBoolean();
		return new CBoolean(false);
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

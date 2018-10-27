package chipmunk.truffle.ast;

import chipmunk.ChipmunkVM;

public class OpDispatcher {

	private Object dispatchHandle;

	protected final ChipmunkVM vm;
	protected final String opName;
	
	public OpDispatcher(ChipmunkVM vm, String opName) {
		this.vm = vm;
		this.opName = opName;
	}
	
	public Object getDispatchHandle() {
		return dispatchHandle;
	}

	public void setDispatchHandle(Object dispatchHandle) {
		this.dispatchHandle = dispatchHandle;
	}
	
	public Object dispatch(Object target, Object[] params) throws Throwable {
		
		if(dispatchHandle == null) {
			Class<?>[] paramTypes = new Class<?>[params.length];
			for (int i = 0; i < params.length; i++) {
				paramTypes[i] = params[i].getClass();
			}
			dispatchHandle = vm.lookupMethod(target, opName, paramTypes);
		}
		return vm.invoke(dispatchHandle, target, params);
	}
}

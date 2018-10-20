package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;

public interface CallInterceptor {

	public Object callAt(ChipmunkVM vm, String methodName, int paramCount);
}

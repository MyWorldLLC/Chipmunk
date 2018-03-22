package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;

public interface CallInterceptor {

	public Object callAt(ChipmunkVM vm, String methodName, int paramCount);
}

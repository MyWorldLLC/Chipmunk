package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;
import chipmunk.RuntimeObject;

public interface CCallable extends RuntimeObject {
	
	public Object call(ChipmunkVM vm, Object[] params);

}

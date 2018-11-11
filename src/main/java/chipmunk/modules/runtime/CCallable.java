package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;

public interface CCallable extends RuntimeObject {
	
	public Object call(ChipmunkVM vm, Byte paramCount);

}

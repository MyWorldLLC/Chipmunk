package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;
import chipmunk.RuntimeObject;

public interface CIterator extends RuntimeObject {
	
	public Object next(ChipmunkVM vm);
	public boolean hasNext(ChipmunkVM vm);

}

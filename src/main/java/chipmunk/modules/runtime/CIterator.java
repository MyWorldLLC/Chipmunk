package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;

public interface CIterator extends RuntimeObject{
	
	public Object next(ChipmunkVM vm);
	public boolean hasNext(ChipmunkVM vm);

}

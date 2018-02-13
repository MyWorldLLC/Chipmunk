package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;

public interface CIterator extends RuntimeObject{
	
	public Object next(ChipmunkVM vm);
	public boolean hasNext(ChipmunkVM vm);

}

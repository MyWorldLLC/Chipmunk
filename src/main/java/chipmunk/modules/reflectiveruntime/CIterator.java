package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;
import chipmunk.reflectors.VMOperator;

public interface CIterator extends VMOperator{
	
	public Object next(ChipmunkVM vm);
	public boolean hasNext(ChipmunkVM vm);

}

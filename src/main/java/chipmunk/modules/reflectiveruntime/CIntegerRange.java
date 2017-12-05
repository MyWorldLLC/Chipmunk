package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;
import chipmunk.reflectors.VMOperator;

public class CIntegerRange implements VMOperator{

	private final int start;
	private final int end;
	private final int step;
	private final boolean inclusive;
	
	public CIntegerRange(int start, int end, int step, boolean inclusive){
		this.start = start;
		this.end = end;
		this.step = step;
		this.inclusive = inclusive;
	}
	
	public int getStart(){
		return start;
	}
	
	public int getEnd(){
		return end;
	}
	
	public int getStep(){
		return step;
	}
	
	public boolean isInclusive(){
		return inclusive;
	}
	
	public CIterator iterator(ChipmunkVM vm){
		return new CIntegerRangeIterator();
	}
	
	private class CIntegerRangeIterator implements CIterator {
		
		private int current;
		
		public CIntegerRangeIterator(){
			current = start;
		}

		@Override
		public CInteger next(ChipmunkVM vm) {
			if(!hasNext(vm)){
				// TODO - error
				throw new IllegalStateException();
			}
			
			int value = current;
			current += step;
			
			vm.traceMem(4);
			return new CInteger(value);
		}

		@Override
		public boolean hasNext(ChipmunkVM vm) {
			if(inclusive){
				return current <= end ? true : false;
			}else{
				return current < end ? true : false;
			}
		}
	}
	
}

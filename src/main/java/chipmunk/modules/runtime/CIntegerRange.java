package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;

public class CIntegerRange implements RuntimeObject{

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
	
	protected class CIntegerRangeIterator implements CIterator {
		
		private int current;
		
		public CIntegerRangeIterator(){
			current = start;
		}

		@Override
		public CInteger next(ChipmunkVM vm) {
			if(!hasNext(vm)){
				throw new IllegalStateException("Iteration past end of range");
			}
			
			int value = current;
			current += step;
			System.out.println("Range iterator: " + this.hashCode() + " returning " + value);
			
			return vm.traceInteger(value);
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

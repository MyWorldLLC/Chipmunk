package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;

public class CFloatRange implements RuntimeObject {

	private final float start;
	private final float end;
	private final float step;
	private final boolean inclusive;
	
	public CFloatRange(float start, float end, float step, boolean inclusive){
		this.start = start;
		this.end = end;
		this.step = step;
		this.inclusive = inclusive;
	}
	
	public float getStart(){
		return start;
	}
	
	public float getEnd(){
		return end;
	}
	
	public float getStep(){
		return step;
	}
	
	public boolean isInclusive(){
		return inclusive;
	}
	
	public CIterator iterator(ChipmunkVM vm){
		return new CFloatRangeIterator();
	}
	
	protected class CFloatRangeIterator implements CIterator {
		
		private float current;
		
		public CFloatRangeIterator(){
			current = start;
		}

		@Override
		public CFloat next(ChipmunkVM vm) {
			if(!hasNext(vm)){
				throw new IllegalStateException("Iteration past end of range");
			}
			
			float value = current;
			current += step;
			
			return vm.traceFloat(value);
		}

		@Override
		public boolean hasNext(ChipmunkVM vm) {
			if(inclusive){
				return current + step <= end ? true : false;
			}else{
				return current + step < end ? true : false;
			}
		}
	}
}

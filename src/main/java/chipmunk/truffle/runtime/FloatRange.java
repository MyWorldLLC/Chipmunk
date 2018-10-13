package chipmunk.truffle.runtime;

import java.util.Iterator;

public class FloatRange {
	
	private final float start;
	private final float end;
	private final float step;
	private final boolean inclusive;
	
	public FloatRange(float start, float end, float step, boolean inclusive){
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
	
	public Iterator<Float> iterator(){
		return new FloatRangeIterator();
	}
	
	private class FloatRangeIterator implements Iterator<Float> {
		
		private float current;
		
		public FloatRangeIterator(){
			current = start;
		}

		@Override
		public Float next() {
			if(!hasNext()){
				// TODO - error
				throw new IllegalStateException();
			}
			
			float value = current;
			current += step;
			
			return value;
		}

		@Override
		public boolean hasNext() {
			if(inclusive){
				return current + step <= end ? true : false;
			}else{
				return current + step < end ? true : false;
			}
		}
	}
}

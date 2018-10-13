package chipmunk.truffle.runtime;

import java.util.Iterator;

public class IntegerRange {

	private final int start;
	private final int end;
	private final int step;
	private final boolean inclusive;
	
	public IntegerRange(int start, int end, int step, boolean inclusive){
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
	
	public Iterator<Integer> iterator(){
		return new IntegerRangeIterator();
	}
	
	private class IntegerRangeIterator implements Iterator<Integer> {
		
		private int current;
		
		public IntegerRangeIterator(){
			current = start;
		}

		@Override
		public Integer next() {
			if(!hasNext()){
				// TODO - error
				throw new IllegalStateException();
			}
			
			int value = current;
			current += step;
			
			return value;
		}

		@Override
		public boolean hasNext() {
			if(inclusive){
				return current <= end ? true : false;
			}else{
				return current < end ? true : false;
			}
		}
	}
	
}

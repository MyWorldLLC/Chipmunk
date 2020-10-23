/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;

public class CIntegerRange {

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
			
			return new CInteger(value); // vm.traceInteger(value);
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

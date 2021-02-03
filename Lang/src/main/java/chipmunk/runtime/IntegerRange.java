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

package chipmunk.runtime;

import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

import java.util.Iterator;

@AllowChipmunkLinkage
public class IntegerRange implements Range<Integer, Integer> {

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
	
	public Integer getStart(){
		return start;
	}
	
	public Integer getEnd(){
		return end;
	}
	
	public Integer getStep(){
		return step;
	}
	
	public boolean isInclusive(){
		return inclusive;
	}
	
	public Iterator<Integer> iterator(){
		return new IntegerRangeIterator();
	}

	@AllowChipmunkLinkage
	protected class IntegerRangeIterator implements Iterator<Integer> {
		
		private int current;
		
		public IntegerRangeIterator(){
			current = start;
		}

		@Override
		public Integer next() {
			if(!hasNext()){
				throw new IllegalStateException("Iteration past end of range");
			}
			
			int value = current;
			current += step;
			
			return value;
		}

		@Override
		public boolean hasNext() {
			if(inclusive){
				return current <= end;
			}else{
				return current < end;
			}
		}
	}
	
}

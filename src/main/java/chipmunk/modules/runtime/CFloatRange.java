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
import chipmunk.RuntimeObject;

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

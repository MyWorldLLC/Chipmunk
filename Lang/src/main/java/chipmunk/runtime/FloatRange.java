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
public class FloatRange implements Range<Float, Float> {

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
	
	public Float getStart(){
		return start;
	}
	
	public Float getEnd(){
		return end;
	}
	
	public Float getStep(){
		return step;
	}
	
	public boolean isInclusive(){
		return inclusive;
	}
	
	public Iterator<Float> iterator(){
		return new FloatRangeIterator();
	}

	@AllowChipmunkLinkage
	protected class FloatRangeIterator implements Iterator<Float> {
		
		private float current;
		
		public FloatRangeIterator(){
			current = start;
		}

		@Override
		public Float next() {
			if(!hasNext()){
				throw new IllegalStateException("Iteration past end of range");
			}
			
			float value = current;
			current += step;
			
			return value;
		}

		@Override
		public boolean hasNext() {
			if(inclusive){
				return current + step <= end;
			}else{
				return current + step < end;
			}
		}
	}
}

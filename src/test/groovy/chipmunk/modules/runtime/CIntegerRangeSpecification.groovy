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

package chipmunk.modules.runtime

import chipmunk.ChipmunkVM
import chipmunk.modules.runtime.CIntegerRange
import spock.lang.Specification

class CIntegerRangeSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()

	def "Create ranges"(int start, int end, int step, boolean inclusive){
		when:
		CIntegerRange range = new CIntegerRange(start, end, step, inclusive)
		
		then:
		range.getStart() == start
		range.getEnd() == end
		range.getStep() == step
		range.isInclusive() == inclusive
		
		where:
		start | end | step | inclusive
		  0   |  5  |   1  |   true
		  0   |  5  |   2  |   false
	}
	
	def "Create range iterators"(int start, int end, int step, boolean inclusive, List expected){
		when:
		def iter = new CIntegerRange(start, end, step, inclusive).iterator(vm)
		def results = []
		while(iter.hasNext(vm)){
			results.add(iter.next(vm).getValue())
		}
		then:
		results == expected
		
		where:
		start | end | step | inclusive | expected
		  0   |  5  |   1  |   true    | [0, 1, 2, 3, 4, 5]
		  0   |  5  |   1  |   false   | [0, 1, 2, 3, 4]
		  1   |  1  |   1  |   false   | []
		  1   |  1  |   1  |   true    | [1]
		  
	}
}

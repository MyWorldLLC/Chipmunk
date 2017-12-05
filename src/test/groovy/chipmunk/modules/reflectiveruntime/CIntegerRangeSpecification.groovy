package chipmunk.modules.reflectiveruntime

import chipmunk.ChipmunkVM
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

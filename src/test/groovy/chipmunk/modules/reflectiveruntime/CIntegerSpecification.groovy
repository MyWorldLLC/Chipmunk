package chipmunk.modules.reflectiveruntime

import chipmunk.ChipmunkVM
import chipmunk.modules.lang.CString
import chipmunk.modules.lang.UnimplementedOperationChipmunk
import spock.lang.Specification

class CIntegerSpecification extends Specification {
	
	ChipmunkVM context = new ChipmunkVM();
	CInteger one = new CInteger(1)
	CInteger two = new CInteger(2)
	CInteger three = new CInteger(3)
	
	CFloat fTwo = new CFloat(2.0)

	def "create CInteger with default value"(){
		expect:
		new CInteger().getValue() == 0
	}
	
	def "create CInteger with specified value"(){
		expect:
		new CInteger(1).getValue() == 1
	}
	
	def "add 1 + 2"(){
		when:
		def result = one.plus(context, two)
		
		then:
		result instanceof CInteger
		result.getValue() == 3
	}
	
	def "add 1 + 2.0"(){
		when:
		def result = one.plus(context, fTwo)
		
		then:
		result instanceof CFloat
		result.getValue() == 3.0
	}
	
	def "perform 1 * 2"(){
		when:
		def result = one.mul(context, two)
		
		then:
		result instanceof CInteger
		result.getValue() == 2
	}
	
	def "perform 1 * 2.0"(){
		when:
		def result = one.mul(context, fTwo)
		
		then:
		result instanceof CFloat
		result.getValue() == 2.0
	}
	
	def "perform 1 / 2"(){
		when:
		def result = one.div(context, two)
		
		then:
		result instanceof CFloat
		result.getValue() == 0.5
	}
	
	def "perform 1 / 2.0"(){
		when:
		def result = one.div(context, fTwo)
		
		then:
		result instanceof CFloat
		result.getValue() == 0.5
	}
	
	def "perform 1 // 2"(){
		when:
		def result = one.fdiv(context, two)
		
		then:
		result instanceof CInteger
		result.getValue() == 0
	}
	
	def "perform 1 // 2.0"(){
		when:
		def result = one.fdiv(context, fTwo)
		
		then:
		result instanceof CInteger
		result.getValue() == 0
	}
}

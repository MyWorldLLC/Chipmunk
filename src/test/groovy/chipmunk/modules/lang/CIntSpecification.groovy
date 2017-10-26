package chipmunk.modules.lang

import spock.lang.Specification

class CIntSpecification extends Specification {
	
	CInt one = new CInt(1)
	CInt two = new CInt(2)
	CInt three = new CInt(3)
	
	CFloat fTwo = new CFloat(2.0)

	def "create CInt with default value"(){
		expect:
		new CInt().getValue() == 0
	}
	
	def "create CInt with specified value"(){
		expect:
		new CInt(1).getValue() == 1
	}
	
	def "add 1 + 2"(){
		when:
		def result = one.__plus__(two)
		
		then:
		result instanceof CInt
		result.getValue() == 3
	}
	
	def "add 1 + 2.0"(){
		when:
		def result = one.__plus__(fTwo)
		
		then:
		result instanceof CFloat
		result.getValue() == 3.0
	}
	
	def "add 1 + CString"(){
		when:
		def result = new CInt(1).__plus__(new CString("foo"))
		
		then:
		def ex = thrown(UnimplementedOperationChipmunk)
		ex.getMessage() == "Undefined operation: cannot perform int + CString"
	}
	
	def "perform 1 * 2"(){
		when:
		def result = one.__mul__(two)
		
		then:
		result instanceof CInt
		result.getValue() == 2
	}
	
	def "perform 1 * 2.0"(){
		when:
		def result = one.__mul__(fTwo)
		
		then:
		result instanceof CFloat
		result.getValue() == 2.0
	}
	
	def "perform 1 / 2"(){
		when:
		def result = one.__div__(two)
		
		then:
		result instanceof CFloat
		result.getValue() == 0.5
	}
	
	def "perform 1 / 2.0"(){
		when:
		def result = one.__div__(fTwo)
		
		then:
		result instanceof CFloat
		result.getValue() == 0.5
	}
	
	def "perform 1 // 2"(){
		when:
		def result = one.__fdiv__(two)
		
		then:
		result instanceof CInt
		result.getValue() == 0
	}
	
	def "perform 1 // 2.0"(){
		when:
		def result = one.__fdiv__(fTwo)
		
		then:
		result instanceof CInt
		result.getValue() == 0
	}
}

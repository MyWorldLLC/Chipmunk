package chipmunk.modules.lang

import spock.lang.Specification

class CIntSpecification extends Specification {

	def "create CInt with default value"(){
		when:
		CInt i = new CInt()
		
		then:
		i.getValue() == 0
	}
	
	def "create CInt with specified value"(){
		when:
		CInt i = new CInt(1)
		
		then:
		i.getValue() == 1
	}
	
	def "add 1 + 2"(){
		when:
		CInt first = new CInt(1)
		CInt second = new CInt(2)
		
		def result = first.__plus__(second)
		
		then:
		result instanceof CInt
		result.getValue() == 3
	}
	
	def "add 1 + 2.0"(){
		when:
		CInt first = new CInt(1)
		CFloat second = new CFloat(2.0)
		
		def result = first.__plus__(second)
		
		then:
		result instanceof CFloat
		result.getValue() == 3.0
	}
	
	def "add 1 + CString"(){
		when:
		CInt first = new CInt(1)
		CString second = new CString("foo")
		
		def result = first.__plus__(second)
		
		then:
		thrown(UnimplementedOperationChipmunk)
	}
}

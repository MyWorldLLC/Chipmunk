package chipmunk.modules.reflectiveruntime

import chipmunk.ChipmunkVM
import spock.lang.Shared
import spock.lang.Specification

class CIntegerSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()
	@Shared CInteger negOne = new CInteger(-1)
	@Shared CInteger negTwo = new CInteger(-2)
	@Shared CInteger zero = new CInteger(0)
	@Shared CInteger one = new CInteger(1)
	@Shared CInteger two = new CInteger(2)
	@Shared CInteger three = new CInteger(3)
	
	@Shared CFloat fOne = new CFloat(1.0)
	@Shared CFloat fTwo = new CFloat(2.0)
	@Shared CFloat fThree = new CFloat(3.0)

	def "create with default value"(){
		expect:
		new CInteger().getValue() == 0
	}
	
	def "create with specified value"(){
		expect:
		new CInteger(1).getValue() == 1
	}
	
	def "addition"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.plus(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		  one | two  | three  | CInteger.class
		  one | fTwo | fThree |  CFloat.class
	   negOne | two  |  one   | CInteger.class
	}
	
	def "subtraction"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.minus(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		three | two  |  one   | CInteger.class
	    three | fTwo |  fOne  |  CFloat.class
	     two  |negOne| three  | CInteger.class
	}
	
	def "multiplication"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.mul(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  two   | CInteger.class
	     one  | fTwo |  fTwo  |  CFloat.class
		 two  |negOne| negTwo | CInteger.class
	}
	
	def "division"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.div(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  fTwo  |  CFloat.class
		 one  | fOne |  fOne  |  CFloat.class
	}
	
	def "floor division"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.fdiv(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  two   |  CInteger.class
		 one  | fTwo |  zero  |  CInteger.class
	}
	
	def "modulo"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.mod(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		three | two  |  one   |  CInteger.class
		three | fTwo |  fOne  |  CFloat.class
	}
	
	def "power"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.pow(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  two   |  CInteger.class
		 two  | zero |  one   |  CInteger.class
		 one  | fTwo |  fOne  |  CFloat.class
	}
	
	def "increment"(Object lh, Object sum, Class<?> type){
		when:
		def result = lh.inc(vm)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  sum   |     type
		 two  |  three |  CInteger.class
		 one  |   two  |  CInteger.class
	}
	
	def "decrement"(Object lh, Object sum, Class<?> type){
		when:
		def result = lh.dec(vm)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  sum   |     type
	   three  |  two   |  CInteger.class
		 two  |  one   |  CInteger.class
	}
	
	def "positive"(Object lh, Object sum, Class<?> type){
		when:
		def result = lh.pos(vm)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  sum   |    type
	   three  | three  | CInteger.class
	   negOne |  one   | CInteger.class
	}
	
	def "negative"(Object lh, Object sum, Class<?> type){
		when:
		def result = lh.neg(vm)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  sum   |    type
	     one  | negOne | CInteger.class
	   negOne |  one   | CInteger.class
	}
}

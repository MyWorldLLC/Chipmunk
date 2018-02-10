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
	
	def "binary xor"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.bxor(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  three |  CInteger.class
		 two  | two  |  zero  |  CInteger.class
		three | one  |   two  |  CInteger.class
	}
	
	def "binary and"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.band(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  zero  |  CInteger.class
		 two  | two  |  two   |  CInteger.class
		three | one  |  one   |  CInteger.class
	}
	
	def "binary or"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.bor(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  three |  CInteger.class
		 two  | two  |  two   |  CInteger.class
		three | one  |  three |  CInteger.class
	}
	
	def "binary negation"(Object lh, Object sum, Class<?> type){
		when:
		def result = lh.bneg(vm)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  sum    |    type
		 one  | negTwo  | CInteger.class
	     zero | negOne  | CInteger.class
	}
	
	def "left shift"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.lshift(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 one  | one  |  two   |  CInteger.class
		 zero | two  |  zero  |  CInteger.class
	}
	
	def "right shift"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.rshift(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  one   |  CInteger.class
		 zero | two  |  zero  |  CInteger.class
	}
	
	def "unsigned right shift"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.urshift(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  one   |  CInteger.class
		 zero | two  |  zero  |  CInteger.class
	}
	
	def "truth"(Object lh, boolean value, Class<?> type){
		when:
		def result = lh.truth(vm)
		
		then:
		result.getClass().equals(type)
		result.getValue() == value
		
		where:
		  lh  |  value   |     type
		 two  |  true    |  CBoolean.class
		 one  |  true    |  CBoolean.class
		 zero |  false   |  CBoolean.class
	}
	
	def "conversion"(Object lh, Class<?> to, Object value, Class<?> type){
		when:
		def result = lh.as(vm, to)
		
		then:
		result.getClass().equals(type)
		result == value
		
		where:
		  lh  |  to             |  value  |     type
		 two  | CInteger.class  |   two   |  CInteger.class
		 one  | CFloat.class    |   fOne  |  CFloat.class
	}
	
	def "comparison"(Object lh, Object rh, Object sum, Class<?> type){
		when:
		def result = lh.compare(vm, rh)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  rh  |  sum   |     type
		 two  | one  |  one   |  CInteger.class
		 zero | two  | negOne |  CInteger.class
		 two  | fOne |  one   |  CInteger.class
	}
	
	def "range"(Object lh, Object other, Object step, Boolean inclusive, Class<?> type){
		when:
		def result = lh.range(vm, other, inclusive)
		
		then:
		result.getClass().equals(type)
		result.getStart() == lh.getValue()
		result.getEnd() == other.getValue()
		result.isInclusive() == inclusive
		result.getStep() == step.getValue()
		
		where:
		  lh  |  other  |  step    | inclusive |     type
		 two  |   one   |  negOne  |   true    | CIntegerRange.class
		 one  |   two   |    one   |   false   | CIntegerRange.class
		 one  |   fTwo  |    one   |   false   | CFloatRange.class
		 two  |   fOne  |  negOne  |   false   | CFloatRange.class
	}
	
	def "hash"(Object lh,  Object sum, Class<?> type){
		when:
		def result = lh.hash(vm)
		
		then:
		result.getClass().equals(type)
		result == sum
		
		where:
		  lh  |  sum   |     type
		 two  |  two   |  CInteger.class
		 zero |  zero  |  CInteger.class
	}
	
	def "to string"(Object lh, Object str, Class<?> type){
		when:
		def result = lh.string(vm)
		
		then:
		result.getClass().equals(type)
		result.stringValue() == str
		
		where:
		  lh  |  str   |     type
		 two  |  "2"   |  CString.class
		 one  |  "1"   |  CString.class
		 zero |  "0"   |  CString.class
	}
}

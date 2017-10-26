package chipmunk

import chipmunk.modules.lang.CInt
import spock.lang.Specification

class ContextSpecification extends Specification {

	def "push and pop 1 item"(){
		when:
		ChipmunkContext context = new ChipmunkContext()
		context.push(new CInt(1))
		
		then:
		context.pop().getValue() == 1
	}
	
	def "push and pop 2 items"(){
		when:
		ChipmunkContext context = new ChipmunkContext()
		context.push(new CInt(1))
		context.push(new CInt(2))
		
		then:
		context.pop().getValue() == 2
		context.pop().getValue() == 1
	}
	
	def "push and dup 1 item"(){
		when:
		ChipmunkContext context = new ChipmunkContext()
		context.push(new CInt(1))
		context.dup(0)
		
		then:
		context.pop().getValue() == 1
		context.pop().getValue() == 1
	}
	
	def "push 2 items and dup 1 item"(){
		when:
		ChipmunkContext context = new ChipmunkContext()
		context.push(new CInt(1))
		context.push(new CInt(2))
		context.dup(1)
		
		then:
		context.pop().getValue() == 1
		context.pop().getValue() == 2
		context.pop().getValue() == 1
	}
	
	def "push 2 items and swap them"(){
		when:
		ChipmunkContext context = new ChipmunkContext()
		context.push(new CInt(1))
		context.push(new CInt(2))
		context.swap(0, 1)
		
		then:
		context.pop().getValue() == 2
		context.pop().getValue() == 1
	}
	
	def "push 3 items and swap 2 items"(){
		when:
		ChipmunkContext context = new ChipmunkContext()
		context.push(new CInt(1))
		context.push(new CInt(2))
		context.push(new CInt(3))
		context.swap(0, 2)
		
		then:
		context.pop().getValue() == 3
		context.pop().getValue() == 2
		context.pop().getValue() == 1
	}
}

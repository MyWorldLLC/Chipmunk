package chipmunk

import chipmunk.modules.reflectiveruntime.CInteger
import chipmunk.reflectors.VMReflector
import spock.lang.Specification

class VMSpecification extends Specification {

	def "push and pop 1 item"(){
		when:
		ChipmunkVM context = new ChipmunkVM()
		context.push(new VMReflector(new CInteger(1)))
		
		then:
		context.pop().getObject().getValue() == 1
	}
	
	def "push and pop 2 items"(){
		when:
		ChipmunkVM context = new ChipmunkVM()
		context.push(new VMReflector(new CInteger(1)))
		context.push(new VMReflector(new CInteger(2)))
		
		then:
		context.pop().getObject().getValue() == 2
		context.pop().getObject().getValue() == 1
	}
	
	def "push and dup 1 item"(){
		when:
		ChipmunkVM context = new ChipmunkVM()
		context.push(new VMReflector(new CInteger(1)))
		context.dup(0)
		
		then:
		context.pop().getObject().getValue() == 1
		context.pop().getObject().getValue() == 1
	}
	
	def "push 2 items and dup 1 item"(){
		when:
		ChipmunkVM context = new ChipmunkVM()
		context.push(new VMReflector(new CInteger(1)))
		context.push(new VMReflector(new CInteger(2)))
		context.dup(1)
		
		then:
		context.pop().getObject().getValue() == 1
		context.pop().getObject().getValue() == 2
		context.pop().getObject().getValue() == 1
	}
	
	def "push 2 items and swap them"(){
		when:
		ChipmunkVM context = new ChipmunkVM()
		context.push(new VMReflector(new CInteger(1)))
		context.push(new VMReflector(new CInteger(2)))
		context.swap(0, 1)
		
		then:
		context.pop().getObject().getValue() == 2
		context.pop().getObject().getValue() == 1
	}
	
	def "push 3 items and swap 2 items"(){
		when:
		ChipmunkVM context = new ChipmunkVM()
		context.push(new VMReflector(new CInteger(1)))
		context.push(new VMReflector(new CInteger(2)))
		context.push(new VMReflector(new CInteger(3)))
		context.swap(0, 2)
		
		then:
		context.pop().getObject().getValue() == 3
		context.pop().getObject().getValue() == 2
		context.pop().getObject().getValue() == 1
	}
}

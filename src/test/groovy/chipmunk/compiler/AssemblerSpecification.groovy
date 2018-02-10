package chipmunk.compiler

import chipmunk.ChipmunkVM
import chipmunk.modules.reflectiveruntime.CInteger
import chipmunk.modules.reflectiveruntime.CMethod
import spock.lang.Specification

class AssemblerSpecification extends Specification {
	
	ChipmunkVM context = new ChipmunkVM()
	ChipmunkAssembler assembler = new ChipmunkAssembler()

	def "Assemble and run 1 + 2"(){
		when:
		assembler.push(new CInteger(1))
		assembler.push(new CInteger(2))
		assembler.add()
		assembler._return()
		
		def result = callMethod()
		
		then:
		result instanceof CInteger
		result.getValue() == 3
	}
	
	def callMethod(){
		
		CMethod method = new CMethod()
		method.setCode(assembler.getCodeSegment())
		method.setConstantPool(assembler.getConstantPool())
		method.setLocalCount(0)
		
		return context.dispatch(method, 0).getObject()
	}
}

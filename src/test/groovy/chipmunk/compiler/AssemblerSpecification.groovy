package chipmunk.compiler

import chipmunk.ChipmunkContext
import chipmunk.modules.lang.CCode
import chipmunk.modules.lang.CInt
import chipmunk.modules.lang.CMethod
import chipmunk.modules.reflectiveruntime.CInteger
import spock.lang.Specification

class AssemblerSpecification extends Specification {
	
	ChipmunkContext context = new ChipmunkContext()
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
		method.setCode(new CCode(assembler.getCodeSegment()))
		method.setConstantPool(assembler.getConstantPool())
		
		return context.dispatch(assembler.getCodeSegment(), 0, 0, assembler.getConstantPool()).getObject()
	}
}

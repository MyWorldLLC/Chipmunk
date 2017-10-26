package chipmunk.compiler

import chipmunk.ChipmunkContext
import chipmunk.modules.lang.CCode
import chipmunk.modules.lang.CInt
import chipmunk.modules.lang.CMethod
import chipmunk.modules.lang.CObject
import spock.lang.Specification

class AssemblerSpecification extends Specification {

	def "Assemble and run 1 + 2"(){
		when:
		ChipmunkAssembler assembler = new ChipmunkAssembler()
		assembler.push(new CInt(1))
		assembler.push(new CInt(2))
		assembler.add()
		assembler._return()
		
		CMethod method = new CMethod()
		method.setCode(new CCode(assembler.getCodeSegment()))
		method.setConstantPool(assembler.getConstantPool())
		
		CObject result = method.__call__(new ChipmunkContext(), 0, false)
		
		then:
		result instanceof CInt
		result.getValue() == 3
	}
}

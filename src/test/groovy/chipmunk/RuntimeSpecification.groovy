package chipmunk

import chipmunk.compiler.ChipmunkCompiler
import spock.lang.Specification

class RuntimeSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()
	ChipmunkCompiler compiler = new ChipmunkCompiler()
	
	def getInputStream(String script){
		return this.getClass().getResourceAsStream(script)
	}
	
	def "Run SimpleMethod.chp"(){
		when:
		ChipmunkScript script = compiler.compile(
			getInputStream("SimpleMethod.chp"), "SimpleMethod.chp")
		script.setEntryCall(script.getModules().get("test").getNamespace().get("main"), [] as Object[])
		def result = vm.run(script)
		
		then:
		result.intValue() == 17
	}

}

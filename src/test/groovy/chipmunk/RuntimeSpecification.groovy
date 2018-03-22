package chipmunk

import chipmunk.compiler.ChipmunkCompiler
import spock.lang.Specification

class RuntimeSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()
	ChipmunkCompiler compiler = new ChipmunkCompiler()
	
	def compileAndRun(String scriptName){
		List modules = compiler.compile(getClass().getResourceAsStream(scriptName), scriptName)
		
		MemoryModuleLoader loader = new MemoryModuleLoader()
		loader.addModules(modules)
		vm.getLoaders().add(loader)
		
		ChipmunkScript script = new ChipmunkScript()
		
		script.setEntryCall("test", "main")
		return vm.run(script)
	}
	
	def "Run SimpleMethod.chp"(){
		when:
		def result = compileAndRun("SimpleMethod.chp")
		
		then:
		result.intValue() == 17
	}
	
	def "Run ModuleWithInitializer.chp"(){
		when:
		def result = compileAndRun("ModuleWithInitializer.chp")
		
		then:
		result.intValue() == 5
	}
	
	def "Run ModuleWithClassInitializer.chp"(){
		when:
		def result = compileAndRun("ModuleWithClassInitializer.chp")
		
		then:
		result.intValue() == 10
	}

	def "Run ClassAndInstanceVariables.chp"(){
		when:
		def result = compileAndRun("ClassAndInstanceVariables.chp")
		
		then:
		result.intValue() == 11
	}
}

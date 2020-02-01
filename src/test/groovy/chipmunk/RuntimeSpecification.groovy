package chipmunk

import chipmunk.compiler.ChipmunkCompiler
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.modules.runtime.CMethod
import spock.lang.Ignore
import spock.lang.Specification

class RuntimeSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()
	ChipmunkCompiler compiler = new ChipmunkCompiler()
	
	def compileAndRun(String scriptName, boolean disassembleOnException = false){
		List modules = compiler.compile(getClass().getResourceAsStream(scriptName), scriptName)
		
		MemoryModuleLoader loader = new MemoryModuleLoader()
		loader.addModules(modules)
		vm.getLoaders().add(loader)
		
		ChipmunkScript script = new ChipmunkScript()
		
		script.setEntryCall("test", "main")
		
		if(!disassembleOnException){
			return vm.run(script)
		}else{
			try{
				return vm.run(script)
			}catch(Exception e){

				for(def module : script.getModules().values()){
					println(ChipmunkDisassembler.disassemble(module))
				}
				
				def sw = new StringWriter()
				e.printStackTrace(new PrintWriter(sw))
				println(sw.toString())
				
				throw e
			}
		}
	}
	
	def "Run SimpleMethod.chp"(){
		when:
		def result = compileAndRun("SimpleMethod.chp", true)
		
		then:
		result.intValue() == 25
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
	
	def "Run SetClassAndInstanceVariables.chp"(){
		when:
		def result = compileAndRun("SetClassAndInstanceVariables.chp")
		
		then:
		result.intValue() == 9
	}
	
	def "Run ModuleImports.chp"(){
		when:
		def result = compileAndRun("ModuleImports.chp")
		
		then:
		result.intValue() == 10
	}
	
	def "Run ModuleStarImport.chp"(){
		when:
		def result = compileAndRun("ModuleStarImport.chp")
		
		then:
		result.intValue() == 10
	}
	
	def "Run ModuleFromImport.chp"(){
		when:
		def result = compileAndRun("ModuleFromImport.chp")
		
		then:
		result.intValue() == 10
	}
	
	def "Run ModuleFromImportStar.chp"(){
		when:
		def result = compileAndRun("ModuleFromImportStar.chp")
		
		then:
		result.intValue() == 10
	}
	
	def "Run ModuleSingleFromImport.chp"(){
		when:
		def result = compileAndRun("ModuleSingleFromImport.chp")
		
		then:
		result.intValue() == 10
	}
	
	def "Run ModuleSingleFromImportAliased.chp"(){
		when:
		def result = compileAndRun("ModuleSingleFromImportAliased.chp")
		
		then:
		result.intValue() == 10
	}
	
	def "Run OverwriteImport.chp"(){
		when:
		def result = compileAndRun("OverwriteImport.chp")
		
		then:
		thrown(AngryChipmunk)
	}
	
	def "Run List.chp"(){
		when:
		def result = compileAndRun("List.chp")
		
		then:
		result.intValue() == 22
	}
	
	def "Run Map.chp"(){
		when:
		def result = compileAndRun("Map.chp")
		
		then:
		result.intValue() == 10
	}
	
	def "Run Polymorphism.chp"(){
		when:
		def result = compileAndRun("Polymorphism.chp")
		
		then:
		result.intValue() == 21
	}
	
	def "Run InnerClasses.chp"(){
		when:
		def result = compileAndRun("InnerClasses.chp")
		
		then:
		result.intValue() == 21
	}
	
	def "Run TryCatch.chp"(){
		when:
		def result = compileAndRun("TryCatch.chp")
		
		then:
		result.intValue() == 2
	}
	
	def "Run Fibonacci.chp"(){
		when:
		def result = compileAndRun("Fibonacci.chp", true)
		
		then:
		result.intValue() == 8
	}
	
	def "Run Mandelbrot.chp"(){
		when:
		def result = compileAndRun("Mandelbrot.chp", true)
		
		then:
		noExceptionThrown()
	}
	
	
	def "Run NestedRangeLoops.chp"(){
		when:
		def result = compileAndRun("NestedRangeLoops.chp")
		
		then:
		result.intValue() == 9
	}
	
	def "Run NestedLoops.chp"(){
		when:
		def result = compileAndRun("NestedLoops.chp")
		
		then:
		result.intValue() == 9
	}

	def "Run StateMachines.chp"(){
		when:
		def result = compileAndRun("StateMachines.chp", true)

		then:
		result.intValue() == 5
	}
	
}

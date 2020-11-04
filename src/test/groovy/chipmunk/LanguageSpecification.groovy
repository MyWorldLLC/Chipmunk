/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk

import chipmunk.binary.BinaryModule
import chipmunk.compiler.ChipmunkCompiler
import chipmunk.compiler.ChipmunkDisassembler
import chipmunk.jvm.CompilationUnit
import chipmunk.runtime.ChipmunkModule
import spock.lang.Ignore
import spock.lang.Specification

class LanguageSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()
	ChipmunkCompiler compiler = new ChipmunkCompiler()
	
	def compileAndRun(String scriptName, boolean disassembleOnException = false){
		BinaryModule[] modules = compiler.compile(getClass().getResourceAsStream(scriptName), scriptName)

		CompilationUnit unit = new CompilationUnit();
		unit.setEntryModule("test")
		unit.setEntryMethodName("main")

		ModuleLoader loader = new ModuleLoader()
		loader.addToLoaded(Arrays.asList(modules))
		unit.setModuleLoader(loader)

		ChipmunkScript script = vm.compileScript(getClass().getResourceAsStream(scriptName), scriptName)
		ChipmunkScript.setCurrentScript(script)

		if(!disassembleOnException){
			return script.run()
		}else{
			try{
				return script.run()
			}catch(Exception e){

				for(def binaryModule : modules){
					println(ChipmunkDisassembler.disassemble(binaryModule))
				}
				
				def sw = new StringWriter()
				e.printStackTrace(new PrintWriter(sw))
				println(sw.toString())
				
				throw e
			}
		}
	}

	@Ignore
	def "Run SimpleMethod.chp"(){
		when:
		def result = compileAndRun("SimpleMethod.chp", true)
		
		then:
		result == 25
	}
	
	def "Run ModuleWithInitializer.chp"(){
		when:
		def result = compileAndRun("ModuleWithInitializer.chp")
		
		then:
		result == 5
	}
	
	def "Run ModuleWithClassInitializer.chp"(){
		when:
		def result = compileAndRun("ModuleWithClassInitializer.chp")
		
		then:
		result == 10
	}

	def "Run ClassAndInstanceVariables.chp"(){
		when:
		def result = compileAndRun("ClassAndInstanceVariables.chp")
		
		then:
		result == 11
	}
	
	def "Run SetClassAndInstanceVariables.chp"(){
		when:
		def result = compileAndRun("SetClassAndInstanceVariables.chp")
		
		then:
		result == 9
	}
	
	def "Run ModuleImports.chp"(){
		when:
		def result = compileAndRun("ModuleImports.chp")
		
		then:
		result == 10
	}
	
	def "Run ModuleStarImport.chp"(){
		when:
		def result = compileAndRun("ModuleStarImport.chp")
		
		then:
		result == 10
	}
	
	def "Run ModuleFromImport.chp"(){
		when:
		def result = compileAndRun("ModuleFromImport.chp")
		
		then:
		result == 10
	}
	
	def "Run ModuleFromImportStar.chp"(){
		when:
		def result = compileAndRun("ModuleFromImportStar.chp")
		
		then:
		result == 10
	}
	
	def "Run ModuleSingleFromImport.chp"(){
		when:
		def result = compileAndRun("ModuleSingleFromImport.chp")
		
		then:
		result == 10
	}
	
	def "Run ModuleSingleFromImportAliased.chp"(){
		when:
		def result = compileAndRun("ModuleSingleFromImportAliased.chp")
		
		then:
		result == 10
	}
	
	def "Run OverwriteImport.chp"(){
		when:
		def result = compileAndRun("OverwriteImport.chp")
		
		then:
		thrown(ChipmunkRuntimeException)
	}
	
	def "Run List.chp"(){
		when:
		def result = compileAndRun("List.chp")
		
		then:
		result == 22
	}
	
	def "Run Map.chp"(){
		when:
		def result = compileAndRun("Map.chp")
		
		then:
		result == 10
	}
	
	def "Run Polymorphism.chp"(){
		when:
		def result = compileAndRun("Polymorphism.chp")
		
		then:
		result == 21
	}
	
	def "Run InnerClasses.chp"(){
		when:
		def result = compileAndRun("InnerClasses.chp")
		
		then:
		result == 21
	}

	def "Run TryCatch.chp"(){
		when:
		def result = compileAndRun("TryCatch.chp")
		
		then:
		result == 2
	}
	
	def "Run Fibonacci.chp"(){
		when:
		def result = compileAndRun("Fibonacci.chp", true)
		
		then:
		result == 8
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
		result == 9
	}
	
	def "Run NestedLoops.chp"(){
		when:
		def result = compileAndRun("NestedLoops.chp")
		
		then:
		result == 9
	}

	def "Run StateMachines.chp"(){
		when:
		def result = compileAndRun("StateMachines.chp", true)

		then:
		result == 5
	}

	def "Run ShortcircuitOperators.chp"(){
		when:
		def result = compileAndRun("ShortcircuitOperators.chp", true)

		then:
		result == true

	}
	
}

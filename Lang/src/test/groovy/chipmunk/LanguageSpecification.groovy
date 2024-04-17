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
import chipmunk.compiler.ChipmunkSource
import chipmunk.compiler.Compilation
import chipmunk.modules.TestModule
import chipmunk.modules.imports.JvmImportModule
import chipmunk.runtime.UnimplementedMethodException
import chipmunk.vm.ChipmunkScript
import chipmunk.vm.ChipmunkVM
import chipmunk.vm.ModuleLoader
import chipmunk.vm.jvm.Uncatchable
import spock.lang.Specification

class StaticAccess {

	public static int FIELD = 1234;

}

class LanguageSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()
	ChipmunkCompiler compiler = new ChipmunkCompiler()
	
	def compileAndRun(String scriptName, boolean disassembleOnException = false){
		return compileAndRunWithArgs(scriptName, null, disassembleOnException)
	}

	def compileAndRunWithArgs(String scriptName, List args = null, boolean disassembleOnException = false){
		ModuleLoader loader = new ModuleLoader()
		loader.registerNativeFactory(JvmImportModule.IMPORT_MODULE_NAME, { new JvmImportModule()})
		loader.registerNativeFactory(TestModule.TEST_MODULE_NAME, { new TestModule() })

		compiler.setModuleLoader(loader)

		Compilation compilation = new Compilation()
		compilation.getSources().add(new ChipmunkSource(getClass().getResourceAsStream(scriptName), scriptName))

		BinaryModule[] modules = compiler.compile(compilation)

		loader.addToLoaded(Arrays.asList(modules))

		ChipmunkScript script = vm.compileScript(modules)
		script.setModuleLoader(loader)
		ChipmunkScript.setCurrentScript(script)

		def argArray = args != null ? args.toArray() : null

		if(!disassembleOnException){
			return argArray == null ? script.run() : script.run(argArray)
		}else{
			try{
				return argArray == null ? script.run() : script.run(argArray)
			}catch(Throwable e){

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

	def "Run ShorthandMethods.chp"(){
		when:
		def result = compileAndRun("ShorthandMethods.chp", true)

		then:
		result == 7
	}
	
	def "Run ModuleWithInitializer.chp"(){
		when:
		def result = compileAndRun("ModuleWithInitializer.chp", true)
		
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
		result == 16
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
		result == 4
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

	def "Run NestedPolymorphism.chp"(){
		when:
		def result = compileAndRun("NestedPolymorphism.chp")

		then:
		result == 3
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

	def "Run TryUncatchable.chp"(){
		when:
		compileAndRun("TryUncatchable.chp")

		then:
		thrown(Uncatchable.class)
	}
	
	def "Run Fibonacci.chp"(){
		when:
		def result = compileAndRun("Fibonacci.chp", true)
		
		then:
		result == 8
	}

	def "Run IfElseExpressions.chp"(){
		when:
		def result = compileAndRun("IfElseExpressions.chp", true)

		then:
		result == [2, 5]
	}
	
	def "Run Mandelbrot.chp"(){
		when:
		def result = compileAndRun("Mandelbrot.chp", true)
		
		then:
		noExceptionThrown()
	}
	
	
	def "Run NestedRangeLoops.chp"(){
		when:
		def result = compileAndRun("NestedRangeLoops.chp", true)
		
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

	def "Run NativeInterop.chp"(){
		when:
		def result = compileAndRun("NativeInterop.chp")

		then:
		result == 'Chipmunk likes Java!'

	}

	def "Run DefaultModuleName.chp"(){
		when:
		def result = compileAndRun("DefaultModuleName.chp")

		then:
		result == 5
	}

	def "Run IsOperator.chp"(){
		when:
		def result = compileAndRun("IsOperator.chp")

		then:
		result == true
	}

	def "Run JavaStatics.chp"(){
		when:
		def result = compileAndRun("JavaStatics.chp")

		then:
		result == 1234
		StaticAccess.FIELD == 5
		noExceptionThrown()
	}

	def "Run Casts.chp"(){
		when:
		def result = compileAndRun("Casts.chp")

		then:
		result == true
	}

	def "Run ListSort.chp"(){
		when:
		def result = compileAndRun("ListSort.chp")

		then:
		result == [1, 2, 3]
	}

	def "Run ListSortWithComparator.chp"(){
		when:
		def result = compileAndRun("ListSortWithComparator.chp")

		then:
		result == [1, 2, 3]
	}

	def "Run ClassMethodBinding.chp"(){
		when:
		def result = compileAndRun("ClassMethodBinding.chp")

		then:
		result == 5
	}

	def "Run ModuleMethodBinding.chp"(){
		when:
		def result = compileAndRun("ModuleMethodBinding.chp")

		then:
		result == 5
	}

	def "Run SimpleMethod.chp"(){
		when:
		def result = compileAndRun("SimpleMethod.chp", true)

		then:
		result == 18
	}

	def "Run BoundMethodArgs.chp"(){
		when:
		def result = compileAndRun("BoundMethodArgs.chp", true)

		then:
		result == [11, 10, 14, 11, 11]
	}

	def "Run UnimplementedMethod.chp"(){
		when:
		def result = compileAndRun("UnimplementedMethod.chp")

		then:
		thrown(UnimplementedMethodException)
	}

	def "Run Upvalues.chp"(){
		when:
		def result = compileAndRun("Upvalues.chp", true)

		then: result == [5, 3, 3, 15, 3]
	}

	def "Proxy SamProxy interface"(){
		when:
		def methodBinding = compileAndRun("ProxySam.chp", true)
		def proxy = vm.proxy(SamProxy.class, methodBinding)
		def result = proxy.getFoo()

		then:
		notThrown(Exception)
		result == "Hello, Proxy!"
	}

	def "Proxy DemoProxy interface"(){
		when:
		def methodBinding = compileAndRun("ProxyDemo.chp", true)
		def proxy = vm.proxy(DemoProxy.class, methodBinding)
		proxy.acceptFoo("Hello, Proxy!")
		def result = proxy.appendFoo("abcd")
		def iResult = proxy.getInt(10)
		def fResult = proxy.getFloat(10.0f)

		then:
		notThrown(Exception)
		result == "abcd1234"
		iResult == 23
		fResult == 32.0f
	}

	def "Run ProxyArguments.chp"(){
		when:
		def result = compileAndRunWithArgs("ProxyArguments.chp", [new SimpleDemoProxyReceiver()], true)

		then:
		result == 7.0f
	}

	def "Run MultilineExpressions.chp"(){
		when:
		def result = compileAndRun("MultilineExpressions.chp", true)

		then:
		result ==  7
	}

	def "Run VariableShadowing.chp"(){
		when:
		def result = compileAndRun("VariableShadowing.chp", true)

		then:
		result ==  20
	}

	def "Run TypeAnnotations.chp"(){
		when:
		def result = compileAndRun("TypeAnnotations.chp", true)

		then:
		result ==  [5, 3, 3, 15, 3]
	}
}

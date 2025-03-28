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

package chipmunk.compiler.codegen

import chipmunk.binary.BinaryMethod
import chipmunk.binary.BinaryModule
import chipmunk.compiler.ChipmunkCompiler
import chipmunk.runtime.CModule
import chipmunk.vm.ChipmunkScript
import chipmunk.vm.ChipmunkVM
import chipmunk.compiler.ChipmunkDisassembler
import chipmunk.vm.ModuleLoader
import chipmunk.vm.jvm.CompilationUnit
import spock.lang.Ignore
import spock.lang.Specification

class MethodVisitorSpecification extends Specification {

	ChipmunkCompiler compiler = new ChipmunkCompiler()
	ChipmunkVM vm = new ChipmunkVM()
	
	def "Parse, generate, and run empty method def"(){
		expect:
		parseAndCall("def method(){}") == null
	}
	
	def "Parse, generate, and run return expression method def"(){
		when:
		def result = parseAndCall("""
			def method(){
				return 1 + 2
			}
		""")
		
		then:
		result instanceof Integer
		result == 3
	}
	
	def "Parse, generate, and run method def with var dec and return"(){
		when:
		def result = parseAndCall("""
			def method(){
				var foobar = 1 + 2
				return foobar
		}""")
		
		then:
		result instanceof Integer
		result == 3
	}
	
	def "Parse, generate, and run method def with undefined var dec and return"(){
		when:
		def result = parseAndCall("""
			def method(){
				var foobar
				return foobar
		}""")
		
		then:
		result == null
	}
	
	def "Parse, generate, and run method def with multiple var decs and return"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 1 + 1
				var v2 = 2
				return v1 + v2
		}""")
		
		then:
		result instanceof Integer
		result == 4
	}
	
	def "Parse, generate, and run method def with local variable assignment"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 1 + 1
				var v2 = v1
				v2 = v1 + 3
				return v2
		}""")
		
		then:
		result instanceof Integer
		result == 5
	}
	
	def "Parse, generate, and run method def with multiple local variable assignments"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1
				var v2
				var v3 = 2 + 3
				v1 = 1
				v2 = v1 + 3
				v3 = 1
				return v1 + v2 + v3
		}""")
		
		then:
		result instanceof Integer
		result == 6
	}
	
	def "Basic if - return in if"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 123
				if(v1 == 123){
					return true
				}
			}
			""")
			
		then:
		result instanceof Boolean
		result == true
	}
	
	def "Basic if - return after if"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 123
				if(v1 != 123){
					return false
				}
				return true
			}
			""")
			
		then:
		result instanceof Boolean
		result == true
	}
	
	def "Multibranch if"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 123
				if(v1 == 123){
					return 1
				}else if(v1 == 124){
					return 2
				}else{
					return 3
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 1
	}
	
	def "Multibranch if - return in second branch"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 124
				if(v1 == 123){
					return 1
				}else if(v1 == 124){
					return 2
				}else{
					return 3
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 2
	}
	
	def "Multibranch if - return in else"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 125
				if(v1 == 123){
					return 1
				}else if(v1 == 124){
					return 2
				}else{
					return 3
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 3
	}
	
	def "Multibranch if - return after else"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 125
				if(v1 == 123){
					return 1
				}else if(v1 == 124){
					return 2
				}else if(v1 == 126){
					return 3
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 125
	}
	
	def "While loop - no iterations"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				while(v1 == 5){
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 0
	}
	
	def "While loop - 5 iterations"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				while(v1 < 5){
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 5
	}
	
	def "For loop over range - 5 iterations"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				for(i in 0..<5){
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 5
	}
	
	def "For loop - break"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				for(i in 0..<5){
					if(i == 3){
						break
					}
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 3
	}
	
	def "For loop - continue"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				for(i in 0..<5){
					if(i == 3){
						continue
					}else{
						v1 = v1 + 1
					}
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 4
	}
	
	def "While loop - break"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				while(v1 < 5){
					v1 = v1 + 1
					if(v1 == 3){
						break
					}
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 3
	}
	
	def "While loop - continue"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				while(v1 < 5){
					if(v1 == 3){
						v1 = v1 + 2
						continue
					}
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof Integer
		result == 5
	}

	def "Lambda call"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = def(){return 1}
				return v1()
			}
			""")
			
		then:
		result instanceof Integer
		result == 1
	}

	def "Lambda call - one parameter"(){
		when:
		def result = parseAndCall("""
			def method(){
				# Use shorthand single expression syntax
				var v1 = def(a) a
				return v1(1)
			}
			""")
			
		then:
		result instanceof Integer
		result == 1
	}

	def "Lambda call - two parameters"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = def(a, b) a + b
				return v1(1, 2)
			}
			""")
			
		then:
		result instanceof Integer
		result == 3
	}
	
	def parseAndCall(String methodBody){

		CModule binary = compiler.compileMethod(methodBody)

		CompilationUnit unit = new CompilationUnit()
		unit.setEntryModule("exp")
		unit.setEntryMethodName("method")

		ModuleLoader loader = new ModuleLoader()
		loader.addToLoaded(binary)
		unit.setModuleLoader(loader)

		ChipmunkScript script = vm.compileScript(unit)
		
		return vm.runAsync(script).get()
	}

}

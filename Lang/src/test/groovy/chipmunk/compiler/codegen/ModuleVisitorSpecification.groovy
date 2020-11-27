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

import chipmunk.binary.BinaryModule
import chipmunk.compiler.ChipmunkCompiler
import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.compiler.parser.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class ModuleVisitorSpecification extends Specification {

	ChipmunkLexer lexer = new ChipmunkLexer()
	ModuleVisitor visitor = new ModuleVisitor()
	
	def "Parse empty module"(){
		when:
		def module = compileModule("")
		
		then:
		module.getName() == ""
		module.getNamespace().size() == 0
	}
	
	def "Parse module with module name and no imports"(){
		when:
		def module = compileModule("module chipmunk.testing")
		
		then:
		module.getName() == "chipmunk.testing"
		module.getNamespace().size() == 0
	}
	
	def "Parse module with module name and imports"(){
		when:
		def module = compileModule(
		"""module chipmunk.testing
			import foobar.*
			import foobar.asdf
			from foobar2 import *
			from foobar2 import asdf2
		""")
		
		then:
		module.getName() == "chipmunk.testing"
		module.getImports().size() == 4
		module.getImports()[0].isImportAll()
		module.getImports()[0].getSymbols() == null
		!module.getImports()[1].isImportAll()
		module.getImports()[1].getSymbols()[0] == "asdf"
		module.getImports()[2].isImportAll()
		module.getImports()[2].getSymbols() == null
		!module.getImports()[3].isImportAll()
		module.getImports()[3].getSymbols()[0] == "asdf2"
		
		module.getNamespace().size() == 0
	}
	
	def "Parse module with no module name and imports"(){
		when:
		def module = compileModule(
		"""import foobar.*
		   import foobar.asdf
		   from foobar2 import *
		   from foobar2 import asdf2
		""")
		
		then:
		module.getName() == ""
		module.getImports().size() == 4
		!module.getImports()[1].isImportAll()
		module.getImports()[1].getSymbols()[0] == "asdf"
		module.getImports()[2].isImportAll()
		module.getImports()[2].getSymbols() == null
		!module.getImports()[3].isImportAll()
		module.getImports()[3].getSymbols()[0] == "asdf2"
		
		module.getNamespace().size() == 0
	}
	
	def "Parse module with no module name and method def"(){
		when:
		def module = compileModule(
		"""def main(){
				return 2
			}
		""")
		
		then:
		module.getName() == ""
		module.getImports().size() == 0
		
		module.getNamespace().size() == 1
	}
	
	def "Parse module with no module name, imports and method def"(){
		when:
		def module = compileModule(
		""" import foobar.*
			from foobar2 import asdf
			
			def main(){
				return 2
			}
		""")
		
		then:
		module.getName() == ""
		module.getImports().size() == 2
		module.getImports()[0].isImportAll()
		!module.getImports()[1].isImportAll()
		module.getImports()[1].getName() == "foobar2"
		module.getImports()[1].getSymbols().size() == 1
		module.getImports()[1].getSymbols()[0] == "asdf"
		
		module.getNamespace().size() == 1
	}
	
	def "Parse module with module name, imports and method def"(){
		when:
		def module = compileModule(
		""" module chipmunk.testing
			import foobar.*
			from foobar2 import asdf
			
			def main(){
				return 2
			}
		""")
		
		then:
		module.getName() == "chipmunk.testing"
		module.getImports().size() == 2
		module.getImports()[0].isImportAll()
		!module.getImports()[1].isImportAll()
		module.getImports()[1].getName() == "foobar2"
		module.getImports()[1].getSymbols().size() == 1
		module.getImports()[1].getSymbols()[0] == "asdf"
		
		module.getNamespace().size() == 1
	}
	
	def "Parse module with module name, imports, method def and var dec"(){
		when:
		def module = compileModule(
		""" module chipmunk.testing
			import foobar.*
			from foobar2 import asdf
			
			var foo

			def main(){
				return 2
			}
		""")
		
		then:
		module.getName() == "chipmunk.testing"
		module.getImports().size() == 2
		module.getImports()[0].isImportAll()
		!module.getImports()[1].isImportAll()
		module.getImports()[1].getName() == "foobar2"
		module.getImports()[1].getSymbols().size() == 1
		module.getImports()[1].getSymbols()[0] == "asdf"
		
		module.getNamespace().size() == 2
	}
	
	/*def "Parse module and run initializer"(){
		when:
		ChipmunkScript script = ChipmunkVM.compile("""
			module chipmunk.testing
			
			var foo = 2

			def main(){
				return foo
			}
		""", "test")
		
		then:
		vm.run(script).intValue() == 2
		script.getModules().get("chipmunk.testing").getNamespace().get("foo").intValue() == 2
	}*/
	
	def compileModule(String expression, String test = ""){

		ChipmunkParser parser = new ChipmunkParser(lexer.lex(expression))
		AstNode root = parser.parseModule()

		ChipmunkCompiler compiler = new ChipmunkCompiler()
		compiler.compile(root)

		BinaryModule module = compiler.compile(root)[0]
		
		if(test != ""){
			println()
			println("============= ${test} =============")
			println(module.toString())
			//println("====Initializer====")
			//println(ChipmunkDisassembler.disassemble(module.getInitializer().getCode(), module.getConstantPool()))
		}
		
		return module
	}

}

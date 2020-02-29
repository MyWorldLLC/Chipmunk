package chipmunk.compiler.codegen

import chipmunk.compiler.ChipmunkDisassembler
import chipmunk.ChipmunkVM
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.runtime.CModule
import spock.lang.Specification

class ModuleVisitorSpecification extends Specification {
	
	ChipmunkVM vm = new ChipmunkVM()
	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	ModuleVisitor visitor = new ModuleVisitor()
	
	def "Parse empty module"(){
		when:
		CModule module = parseModule("")
		
		then:
		module.getName() == ""
		module.getNamespace().names().size() == 0
	}
	
	def "Parse module with module name and no imports"(){
		when:
		CModule module = parseModule("module chipmunk.testing")
		
		then:
		module.getName() == "chipmunk.testing"
		module.getNamespace().names().size() == 0
	}
	
	def "Parse module with module name and imports"(){
		when:
		CModule module = parseModule(
		"""module chipmunk.testing
			import foobar.*
			import foobar.asdf
			from foobar2 import *
			from foobar2 import asdf2
		""")
		
		then:
		module.getName() == "chipmunk.testing"
		module.getImports().size() == 5
		module.getImports()[1].isImportAll() == true
		module.getImports()[1].getSymbols().size() == 0
		module.getImports()[2].isImportAll() == false
		module.getImports()[2].getSymbols()[0] == "asdf"
		module.getImports()[3].isImportAll() == true
		module.getImports()[3].getSymbols().size()== 0
		module.getImports()[4].isImportAll() == false
		module.getImports()[4].getSymbols()[0] == "asdf2"
		
		module.getNamespace().names().size() == 0
	}
	
	def "Parse module with no module name and imports"(){
		when:
		CModule module = parseModule(
		"""import foobar.*
		   import foobar.asdf
		   from foobar2 import *
		   from foobar2 import asdf2
		""")
		
		then:
		module.getName() == ""
		module.getImports().size() == 5
		module.getImports()[1].isImportAll() == true
		module.getImports()[1].getSymbols().size() == 0
		module.getImports()[2].isImportAll() == false
		module.getImports()[2].getSymbols()[0] == "asdf"
		module.getImports()[3].isImportAll() == true
		module.getImports()[3].getSymbols().size()== 0
		module.getImports()[4].isImportAll() == false
		module.getImports()[4].getSymbols()[0] == "asdf2"
		
		module.getNamespace().names().size() == 0
	}
	
	def "Parse module with no module name and method def"(){
		when:
		CModule module = parseModule(
		"""def main(){
				return 2
			}
		""")
		
		then:
		module.getName() == ""
		module.getImports().size() == 1
		
		module.getNamespace().names().size() == 1
		vm.dispatch(module.getNamespace().get("main"), 0).intValue() == 2
	}
	
	def "Parse module with no module name, imports and method def"(){
		when:
		CModule module = parseModule(
		""" import foobar.*
			from foobar2 import asdf
			
			def main(){
				return 2
			}
		""")
		
		then:
		module.getName() == ""
		module.getImports().size() == 3
		module.getImports()[1].isImportAll() == true
		module.getImports()[2].isImportAll() == false
		module.getImports()[2].getName() == "foobar2"
		module.getImports()[2].getSymbols().size() == 1
		module.getImports()[2].getSymbols()[0] == "asdf"
		
		module.getNamespace().names().size() == 1
		vm.dispatch(module.getNamespace().get("main"), 0).intValue() == 2
	}
	
	def "Parse module with module name, imports and method def"(){
		when:
		CModule module = parseModule(
		""" module chipmunk.testing
			import foobar.*
			from foobar2 import asdf
			
			def main(){
				return 2
			}
		""")
		
		then:
		module.getName() == "chipmunk.testing"
		module.getImports().size() == 3
		module.getImports()[1].isImportAll() == true
		module.getImports()[2].isImportAll() == false
		module.getImports()[2].getName() == "foobar2"
		module.getImports()[2].getSymbols().size() == 1
		module.getImports()[2].getSymbols()[0] == "asdf"
		
		module.getNamespace().names().size() == 1
		vm.dispatch(module.getNamespace().get("main"), 0).intValue() == 2
	}
	
	def "Parse module with module name, imports, method def and var dec"(){
		when:
		CModule module = parseModule(
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
		module.getImports().size() == 3
		module.getImports()[1].isImportAll() == true
		module.getImports()[2].isImportAll() == false
		module.getImports()[2].getName() == "foobar2"
		module.getImports()[2].getSymbols().size() == 1
		module.getImports()[2].getSymbols()[0] == "asdf"
		
		module.getNamespace().names().size() == 2
		vm.dispatch(module.getNamespace().get("main"), 0).intValue() == 2
	}
	
	def "Parse module and run initializer"(){
		when:
		CModule module = parseModule(
		""" module chipmunk.testing
			
			var foo = 2

			def main(){
				return foo
			}
		""")
		
		then:
		module.getName() == "chipmunk.testing"
		module.getNamespace().names().size() == 2
		vm.dispatch(module.getInitializer(), 0)
		vm.dispatch(module.getNamespace().get("main"), 0).intValue() == 2
		module.getNamespace().get("foo").intValue() == 2
	}
	
	def parseModule(String expression, String test = ""){
		
		parser = new ChipmunkParser(lexer.lex(expression))
		AstNode root = parser.parseModule()
		root.visit(new SymbolTableBuilderVisitor())
		root.visit(visitor)
		
		
		CModule module = visitor.getModule()
		
		if(test != ""){
			println()
			println("============= ${test} =============")
			println(module.toString())
			println("====Initializer====")
			println(ChipmunkDisassembler.disassemble(module.getInitializer().getCode(), module.getInitializer().getConstantPool()))
		}
		
		return module
	}

}

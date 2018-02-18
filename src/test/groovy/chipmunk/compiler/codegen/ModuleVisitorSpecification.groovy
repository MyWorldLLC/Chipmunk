package chipmunk.compiler.codegen

import chipmunk.ChipmunkDisassembler
import chipmunk.ChipmunkVM
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.reflectiveruntime.CMethod
import chipmunk.modules.reflectiveruntime.CModule
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
		}
		
		return module
	}

}

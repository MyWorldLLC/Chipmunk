package chipmunk.compiler.codegen

import chipmunk.ChipmunkDisassembler
import chipmunk.ChipmunkVM
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.reflectiveruntime.CClass
import chipmunk.modules.reflectiveruntime.CModule
import chipmunk.modules.reflectiveruntime.CObject
import spock.lang.Specification

class ClassVisitorSpecification extends Specification {

	ChipmunkVM vm = new ChipmunkVM()
	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	ClassVisitor visitor = new ClassVisitor(new CModule(""))
	
	def "Parse empty class"(){
		when:
		CClass cClass = parseClass("""
			class Chipmunk {}
		""")
		
		then:
		cClass.getName() == "Chipmunk"
		cClass.getAttributes().names().size() == 0
		cClass.getInstanceAttributes().names().size() == 1
		cClass.getSharedInitializer() != null
		cClass.getInstanceInitializer() != null
	}
	
	def "Parse class with shared variable and initialize"(){
		when:
		CClass cClass = parseClass("""
			class Chipmunk {
				shared var foo = 2
			}
		""")
		
		vm.dispatch(cClass.getSharedInitializer(), 0)
		
		then:
		cClass.getName() == "Chipmunk"
		cClass.getAttributes().names().size() == 1
		cClass.getAttributes().get("foo").intValue() == 2
		cClass.getInstanceAttributes().names().size() == 1
		cClass.getInstanceInitializer() != null
	}
	
	def "Parse class with instance variable and initialize"(){
		when:
		CClass cClass = parseClass("""
			class Chipmunk {
				var foo = 2
			}
		""", "instance var init")
		
		CObject instance = cClass.call(vm, (byte)0)
		
		then:
		instance.getCClass().getName() == "Chipmunk"
		instance.getAttributes().names().size() == 2
		instance.getAttributes().get("foo").intValue() == 2
	}
	
	def parseClass(String expression, String test = ""){
		
		parser = new ChipmunkParser(lexer.lex(expression))
		AstNode root = parser.parseClassDef()
		root.visit(new SymbolTableBuilderVisitor())
		root.visit(visitor)
		
		
		CClass cClass = visitor.getCClass()
		
		if(test != ""){
			println()
			println("============= ${test} =============")
			println(cClass.getName().toString())
			println("====Instance Initializer====")
			println(ChipmunkDisassembler.disassemble(cClass.getInstanceInitializer().getCode(), cClass.getInstanceInitializer().getConstantPool()))
		}
		
		return cClass
	}
}

package chipmunk.compiler.codegen

import chipmunk.ChipmunkContext
import chipmunk.compiler.ChipmunkAssembler
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.SymbolTable
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.lang.CBoolean
import chipmunk.modules.lang.CCode
import chipmunk.modules.lang.CFloat
import chipmunk.modules.lang.CInt
import chipmunk.modules.lang.CMethod
import chipmunk.modules.lang.CString
import spock.lang.Specification

class ExpressionVisitorSpecification extends Specification {

	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	ChipmunkAssembler assembler = new ChipmunkAssembler()
	ExpressionVisitor visitor = new ExpressionVisitor(assembler, new SymbolTable())
	
	def "Evaluate boolean literal true"(){
		when:
		def result = parseAndCall("true")
		
		then:
		result instanceof CBoolean
		result.getValue() == true
	}
	
	def "Evaluate boolean literal false"(){
		when:
		def result = parseAndCall("false")
		
		then:
		result instanceof CBoolean
		result.getValue() == false
	}
	
	def "Evaluate int literal 0"(){
		when:
		def result = parseAndCall("0")
		
		then:
		result instanceof CInt
		result.getValue() == 0
	}
	
	def "Evaluate int literal -1"(){
		when:
		def result = parseAndCall("-1")
		
		then:
		result instanceof CInt
		result.getValue() == -1
	}
	
	def "Evaluate float literal 0.0"(){
		when:
		def result = parseAndCall("0.0")
		
		then:
		result instanceof CFloat
		result.getValue() == 0.0
	}
	
	def "Evaluate float literal -1.0"(){
		when:
		def result = parseAndCall("-1.0")
		
		then:
		result instanceof CFloat
		result.getValue() == -1.0
	}
	
	def "Evaluate hex literal 0xA6"(){
		when:
		def result = parseAndCall("0xA6")
		
		then:
		result instanceof CInt
		result.getValue() == 0xA6
	}
	
	def "Evaluate oct literal 0o12"(){
		when:
		def result = parseAndCall("0o12")
		
		then:
		result instanceof CInt
		result.getValue() == 012
	}
	
	def "Evaluate binary literal 0b101"(){
		when:
		def result = parseAndCall("0b101")
		
		then:
		result instanceof CInt
		result.getValue() == 0b101
	}
	
	def "Evaluate String literal \"Hello, World!\""(){
		when:
		def result = parseAndCall("\"Hello, World!\"")
		
		then:
		result instanceof CString
		result.getValue() == "\"Hello, World!\""
	}

	def "Generate and run code for 1 + 2"(){
		when:
		def result = parseAndCall("1 + 2")

		then:
		result instanceof CInt
		result.getValue() == 3
	}
	
	def "Generate and run code for +1"(){
		when:
		def result = parseAndCall("+1")

		then:
		result instanceof CInt
		result.getValue() == 1
	}
	
	def "Generate and run code for -1"(){
		when:
		def result = parseAndCall("-1")

		then:
		result instanceof CInt
		result.getValue() == -1
	}

	def "Generate and run code for +-1"(){
		when:
		def result = parseAndCall("+-1")

		then:
		result instanceof CInt
		result.getValue() == 1
	}
	
	def "Generate and run code for 1 * 2"(){
		when:
		def result = parseAndCall("1 * 2")

		then:
		result instanceof CInt
		result.getValue() == 2
	}

	def "Generate and run code for 1 / 2"(){
		when:
		def result = parseAndCall("1 / 2")

		then:
		result instanceof CFloat
		result.getValue() == 0.5
	}

	def "Generate and run code for 1 // 2"(){
		when:
		def result = parseAndCall("1 // 2")

		then:
		result instanceof CInt
		result.getValue() == 0
	}

	def "Generate and run code for 3 % 2"(){
		when:
		def result = parseAndCall("3 % 2")

		then:
		result instanceof CInt
		result.getValue() == 1
	}
	
	def "Generate and run code for 2**1**2"(){
		when:
		def result = parseAndCall("2**1**2")

		then:
		result instanceof CInt
		result.getValue() == 2
	}

	def parseAndCall(String expression){
		parser = new ChipmunkParser(lexer.lex(expression))
		AstNode root = parser.parseExpression()
		root.visit(visitor)
		assembler._return()

		CMethod method = new CMethod()
		method.setCode(new CCode(assembler.getCodeSegment()))
		method.setConstantPool(assembler.getConstantPool())

		return method.__call__(new ChipmunkContext(), 0, false)
	}
}

package chipmunk.compiler.codegen

import chipmunk.ChipmunkDisassembler
import chipmunk.ChipmunkVM
import chipmunk.compiler.ChipmunkAssembler
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.runtime.CBoolean
import chipmunk.modules.runtime.CFloat
import chipmunk.modules.runtime.CInteger
import chipmunk.modules.runtime.CList
import chipmunk.modules.runtime.CMap
import chipmunk.modules.runtime.CMethod
import chipmunk.modules.runtime.CModule
import chipmunk.modules.runtime.CString
import spock.lang.Specification

class ExpressionVisitorSpecification extends Specification {

	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	Codegen codegen = new Codegen(new CModule())
	ChipmunkAssembler assembler = codegen.getAssembler()
	ChipmunkVM context = new ChipmunkVM()
	ExpressionVisitor visitor = new ExpressionVisitor(codegen)
	
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
		result instanceof CInteger
		result.getValue() == 0
	}
	
	def "Evaluate int literal -1"(){
		when:
		def result = parseAndCall("-1")
		
		then:
		result instanceof CInteger
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
		result instanceof CInteger
		result.getValue() == 0xA6
	}
	
	def "Evaluate oct literal 0o12"(){
		when:
		def result = parseAndCall("0o12")
		
		then:
		result instanceof CInteger
		result.getValue() == 012
	}
	
	def "Evaluate binary literal 0b101"(){
		when:
		def result = parseAndCall("0b101")
		
		then:
		result instanceof CInteger
		result.getValue() == 0b101
	}
	
	def "Test escaped and unescaped strings"(def source, def expected){
		when:
		def result = parseAndCall(source)
		
		then:
		result instanceof CString
		result.stringValue() == expected
		
		where:
		source              		 	| 	expected
		'"Double quotes"'			 	| "Double quotes"
		"'Single quotes'"			 	| "Single quotes"
		"'\"Escaped double quotes\"'"	| '"Escaped double quotes"'
		'"\'Escaped single quotes\'"' 	| "'Escaped single quotes'"
		"'\\\"Hello, World!\\\"'" 	 	| '\"Hello, World!\"'
		"'\\\'Hello, World!\\\''" 	 	| "\'Hello, World!\'"
		'"\\"Hello, \\" World!\\""' 	| '"Hello, " World!"'
		"'\\'Hello, \\' World!\\''" 	| "'Hello, ' World!'"
	}

	def "Generate and run code for 1 + 2"(){
		when:
		def result = parseAndCall("1 + 2")

		then:
		result instanceof CInteger
		result.getValue() == 3
	}
	
	def "Generate and run code for +1"(){
		when:
		def result = parseAndCall("+1")

		then:
		result instanceof CInteger
		result.getValue() == 1
	}
	
	def "Generate and run code for -1"(){
		when:
		def result = parseAndCall("-1")

		then:
		result instanceof CInteger
		result.getValue() == -1
	}

	def "Generate and run code for +-1"(){
		when:
		def result = parseAndCall("+-1")

		then:
		result instanceof CInteger
		result.getValue() == 1
	}
	
	def "Generate and run code for 1 * 2"(){
		when:
		def result = parseAndCall("1 * 2")

		then:
		result instanceof CInteger
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
		result instanceof CInteger
		result.getValue() == 0
	}

	def "Generate and run code for 3 % 2"(){
		when:
		def result = parseAndCall("3 % 2")

		then:
		result instanceof CInteger
		result.getValue() == 1
	}
	
	def "Generate and run code for 2**1**2"(){
		when:
		def result = parseAndCall("2**1**2")

		then:
		result instanceof CInteger
		result.getValue() == 2
	}
	
	def "Generate and run code for true && true"(){
		when:
		def result = parseAndCall("true && true")

		then:
		result instanceof CBoolean
		result.getValue() == true
	}
	
	def "Generate and run code for true && false"(){
		when:
		def result = parseAndCall("true && false")

		then:
		result instanceof CBoolean
		result.getValue() == false
	}
	
	def "Generate and run code for complex comparison"(){
		when:
		def result = parseAndCall("2*2 + 3*3 <= 4*4 && 4 < 5", "Complex comparison")

		then:
		result instanceof CBoolean
		result.getValue() == true
	}
	
	def "Evaluate []"(){
		when:
		def result = parseAndCall("[]")
		
		then:
		result instanceof CList
		result.size() == 0
	}
	
	def "Evaluate {}"(){
		when:
		def result = parseAndCall("{}")
		
		then:
		result instanceof CMap
		result.size() == 0
	}
	
	def "Evaluate [1, 2, 3]"(){
		when:
		def result = parseAndCall("[1, 2, 3]")
		
		then:
		result instanceof CList
		result.size().intValue() == 3
		result.get(0).intValue() == 1
		result.get(1).intValue() == 2
		result.get(2).intValue() == 3
	}
	
	def "Evaluate {1:2, 3:4, \"foo\":'bar'}"(){
		when:
		def result = parseAndCall("""{1:2, 3:4, "foo" : 'bar'}""")
		
		then:
		result instanceof CMap
		result.size().intValue() == 3
		result.get(new CInteger(1)).intValue() == 2
		result.get(new CInteger(3)).intValue() == 4
		result.get(new CString("foo")).stringValue() == "bar"
	}
	
	def "Evaluate {1:2, 3:4}[3]"(){
		when:
		def result = parseAndCall("""{1:2, 3:4}[3]""")
		
		then:
		result instanceof CInteger
		result.intValue() == 4
	}
	
	def "Evaluate [1, 2, 3][1]"(){
		when:
		def result = parseAndCall("""[1, 2, 3][1]""")
		
		then:
		result instanceof CInteger
		result.intValue() == 2
	}
	
	def "Evaluate [1, 2, 3][1] = 0"(){
		when:
		def result = parseAndCall("""[1, 2, 3][1] = 0""")
		
		then:
		result instanceof CInteger
		result.intValue() == 2
	}
	
	def "Evaluate {1:2, 3:4}[1] = 0"(){
		when:
		def result = parseAndCall("""{1:2, 3:4}[1] = 0""")
		
		then:
		result instanceof CInteger
		result.intValue() == 2
	}
	
	def parseAndCall(String expression, String test = ""){
		parser = new ChipmunkParser(lexer.lex(expression))
		AstNode root = parser.parseExpression()
		root.visit(visitor)
		assembler._return()
		
		CMethod method = assembler.makeMethod()
		method.setInstructions(assembler.getCodeSegment())
		method.setConstantPool(assembler.getConstantPool().toArray())
		method.setLocalCount(0)
		
		if(test != ""){
			println()
			println(root.toString())
			println("============= ${test} =============")
			println("Local Count: ${method.getLocalCount()}")
			println(ChipmunkDisassembler.disassemble(method.getCode(), method.getConstantPool()))
		}

		return context.dispatch(method, 0)
	}
}

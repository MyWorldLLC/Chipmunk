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
import chipmunk.compiler.ChipmunkDisassembler
import chipmunk.vm.ChipmunkVM
import chipmunk.compiler.assembler.ChipmunkAssembler
import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.compiler.parser.ChipmunkParser
import spock.lang.Specification

class ExpressionVisitorSpecification extends Specification {

	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	Codegen codegen = new Codegen(new BinaryModule("test"))
	ChipmunkAssembler assembler = codegen.getAssembler()
	ChipmunkVM vm = new ChipmunkVM(ChipmunkVM.SecurityMode.UNRESTRICTED)
	ExpressionVisitor visitor = new ExpressionVisitor(codegen)
	
	def "Evaluate boolean literal true"(){
		when:
		def result = parseAndCall("true")
		
		then:
		result instanceof Boolean
		result == true
	}
	
	def "Evaluate boolean literal false"(){
		when:
		def result = parseAndCall("false")
		
		then:
		result instanceof Boolean
		result == false
	}
	
	def "Evaluate int literal 0"(){
		when:
		def result = parseAndCall("0")
		
		then:
		result instanceof Integer
		result == 0
	}
	
	def "Evaluate int literal -1"(){
		when:
		def result = parseAndCall("-1")
		
		then:
		result instanceof Integer
		result == -1
	}
	
	def "Evaluate float literal 0.0"(){
		when:
		def result = parseAndCall("0.0")
		
		then:
		result instanceof Float
		result == 0.0
	}
	
	def "Evaluate float literal -1.0"(){
		when:
		def result = parseAndCall("-1.0")
		
		then:
		result instanceof Float
		result == -1.0
	}
	
	def "Evaluate hex literal 0xA6"(){
		when:
		def result = parseAndCall("0xA6")
		
		then:
		result instanceof Integer
		result == 0xA6
	}
	
	def "Evaluate oct literal 0o12"(){
		when:
		def result = parseAndCall("0o12")
		
		then:
		result instanceof Integer
		result == 012
	}
	
	def "Evaluate binary literal 0b101"(){
		when:
		def result = parseAndCall("0b101")
		
		then:
		result instanceof Integer
		result == 0b101
	}
	
	def "Test escaped and unescaped strings"(def source, def expected){
		when:
		def result = parseAndCall(source)
		
		then:
		result instanceof String
		result == expected
		
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
		result instanceof Integer
		result == 3
	}
	
	def "Generate and run code for +1"(){
		when:
		def result = parseAndCall("+1")

		then:
		result instanceof Integer
		result == 1
	}
	
	def "Generate and run code for -1"(){
		when:
		def result = parseAndCall("-1")

		then:
		result instanceof Integer
		result == -1
	}

	def "Generate and run code for +-1"(){
		when:
		def result = parseAndCall("+-1")

		then:
		result instanceof Integer
		result == 1
	}
	
	def "Generate and run code for 1 * 2"(){
		when:
		def result = parseAndCall("1 * 2")

		then:
		result instanceof Integer
		result == 2
	}

	def "Generate and run code for 1 / 2"(){
		when:
		def result = parseAndCall("1 / 2")

		then:
		result instanceof Float
		result == 0.5
	}

	def "Generate and run code for 1 // 2"(){
		when:
		def result = parseAndCall("1 // 2")

		then:
		result instanceof Integer
		result == 0
	}

	def "Generate and run code for 3 % 2"(){
		when:
		def result = parseAndCall("3 % 2")

		then:
		result instanceof Integer
		result == 1
	}
	
	def "Generate and run code for 2**1**2"(){
		when:
		def result = parseAndCall("2**1**2")

		then:
		result instanceof Integer
		result == 2
	}
	
	def "Generate and run code for true && true"(){
		when:
		def result = parseAndCall("true && true")

		then:
		result instanceof Boolean
		result == true
	}
	
	def "Generate and run code for true && false"(){
		when:
		def result = parseAndCall("true && false")

		then:
		result instanceof Boolean
		result == false
	}

	def "Generate and run code for true || true"(){
		when:
		def result = parseAndCall("true || true")

		then:
		result instanceof Boolean
		result == true
	}

	def "Generate and run code for true || false"(){
		when:
		def result = parseAndCall("true || false")

		then:
		result instanceof Boolean
		result == true
	}

	def "Generate and run code for false || false"(){
		when:
		def result = parseAndCall("false || false")

		then:
		result instanceof Boolean
		result == false
	}
	
	def "Generate and run code for complex comparison"(){
		when:
		def result = parseAndCall("2*2 + 3*3 <= 4*4 && 4 < 5")

		then:
		result instanceof Boolean
		result == true
	}
	
	def "Evaluate []"(){
		when:
		def result = parseAndCall("[]")
		
		then:
		result instanceof List
		result.size() == 0
	}
	
	def "Evaluate {}"(){
		when:
		def result = parseAndCall("{}")
		
		then:
		result instanceof Map
		result.size() == 0
	}
	
	def "Evaluate [1, 2, 3]"(){
		when:
		def result = parseAndCall("[1, 2, 3]")
		
		then:
		result instanceof List
		result.size() == 3
		result.get(0) == 1
		result.get(1) == 2
		result.get(2) == 3
	}
	
	def "Evaluate {1:2, 3:4, \"foo\":'bar'}"(){
		when:
		def result = parseAndCall("""{1:2, 3:4, "foo" : 'bar'}""")
		
		then:
		result instanceof Map
		result.size() == 3
		result.get(1) == 2
		result.get(3) == 4
		result.get("foo") == "bar"
	}
	
	def "Evaluate {1:2, 3:4}[3]"(){
		when:
		def result = parseAndCall("""{1:2, 3:4}[3]""")
		
		then:
		result instanceof Integer
		result == 4
	}
	
	def "Evaluate [1, 2, 3][1]"(){
		when:
		def result = parseAndCall("""[1, 2, 3][1]""")
		
		then:
		result instanceof Integer
		result == 2
	}
	
	def "Evaluate [1, 2, 3][1] = 0"(){
		when:
		def result = parseAndCall("""[1, 2, 3][1] = 0""")
		
		then:
		result instanceof Integer
		result == 2
	}
	
	def "Evaluate {1:2, 3:4}[1] = 0"(){
		when:
		def result = parseAndCall("""{1:2, 3:4}[1] = 0""")
		
		then:
		result instanceof Integer
		result == 2
	}
	
	def parseAndCall(String expression, String test = ""){
		
		if(test != ""){
			println()
			println("============= ${test} =============")
			println("Local Count: ${method.getLocalCount()}")
			println(ChipmunkDisassembler.disassemble(method.getCode(), method.getConstantPool()))
		}

		return vm.eval(expression)
	}
}

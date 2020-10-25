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


import chipmunk.binary.BinaryClass
import chipmunk.binary.BinaryModule
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import chipmunk.compiler.ast.transforms.SymbolTableBuilderVisitor
import spock.lang.Specification

class ClassVisitorSpecification extends Specification {

	ClassVisitor visitor = new ClassVisitor(new BinaryModule("test"))
	
	def "Parse empty class"(){
		when:
		BinaryClass cls = parseClass("""
			class Chipmunk {}
		""")
		
		then:
		cls.getName() == "Chipmunk"
		cls.getInstanceNamespace().getEntries().size() == 0
		cls.getSharedNamespace().getEntries().size() == 0
	}
	
	def "Parse class with shared variable"(){
		when:
		BinaryClass cls = parseClass("""
			class Chipmunk {
				shared var foo = 2
			}
		""")
		
		then:
		cls.getName() == "Chipmunk"
		cls.getInstanceNamespace().getEntries().size() == 0
		cls.getSharedNamespace().getEntries().size() == 1
	}
	
	def "Parse class with instance variable"(){
		when:
		BinaryClass cls = parseClass("""
			class Chipmunk {
				var foo = 2
			}
		""")
		
		then:
		cls.getName() == "Chipmunk"
		cls.getInstanceNamespace().getEntries().size() == 1
		cls.getSharedNamespace().getEntries().size() == 0
	}
	
	def "Parse class with shared and instance variables"(){
		when:
		BinaryClass cls = parseClass("""
			class Chipmunk {
				shared var foo = 2
				var bar = 3
			}
		""")
		
		then:
		cls.getName() == "Chipmunk"
		cls.getInstanceNamespace().getEntries().size() == 1
		cls.getSharedNamespace().getEntries().size() == 1
	}
	
	def "Parse class with instance variable and empty constructor"(){
		when:
		BinaryClass cls = parseClass("""
			class Chipmunk {
				var foo = 2
				
				def Chipmunk(){}
			}
		""")
		
		then:
		cls.getName() == "Chipmunk"
		cls.getInstanceNamespace().getEntries().size() == 2
		cls.getSharedNamespace().getEntries().size() == 0
	}
	
	def "Parse class with instance variable and non-empty constructor"(){
		when:
		BinaryClass cls = parseClass("""
			class Chipmunk {
				var foo = 2
				
				def Chipmunk(){
					foo = foo + 3
				}
			}
		""")
		
		then:
		cls.getName() == "Chipmunk"
		cls.getInstanceNamespace().getEntries().size() == 2
		cls.getSharedNamespace().getEntries().size() == 0
	}
	
	def "Parse class with shared & instance variables and non-empty constructor"(){
		when:
		BinaryClass cls = parseClass("""
			class Chipmunk {
				shared var foo = 2
				var bar = 3
				
				def Chipmunk(){
					foo = foo + 3
					bar = bar + 3
				}
			}
		""")
		
		then:
		cls.getName() == "Chipmunk"
		cls.getInstanceNamespace().getEntries().size() == 2
		cls.getSharedNamespace().getEntries().size() == 1
	}
	
	def parseClass(String expression, String test = ""){

		ChipmunkLexer lexer = new ChipmunkLexer()
		ChipmunkParser parser = new ChipmunkParser(lexer.lex(expression))
		AstNode root = parser.parseClassDef()
		root.visit(new SymbolTableBuilderVisitor())
		root.visit(visitor)


		BinaryClass cls = visitor.getBinaryClass()
		
		if(test != ""){
			println()
			println("============= ${test} =============")
			println(ChipmunkDisassembler.disassemble(cls))
		}
		
		return cls
	}
}

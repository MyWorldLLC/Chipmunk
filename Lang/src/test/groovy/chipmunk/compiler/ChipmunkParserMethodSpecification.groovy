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

package chipmunk.compiler

import chipmunk.compiler.ast.MethodNode
import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.compiler.parser.ChipmunkParser
import spock.lang.Specification

class ChipmunkParserMethodSpecification extends Specification {

	def "parse def foo(){}"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("def foo(){}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo)"
	}
	
	def "parse def foo(arg1){}"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("def foo(arg1){}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo (vardec arg1))"
	}
	
	def "parse def foo(arg1, arg2){}"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("def foo(arg1, arg2){}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo (vardec arg1) (vardec arg2))"
	}
	
	def "parse method def with single var body"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""def foo(){
					var asdf = 1
				}
			""")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo (vardec asdf (literal 1)))"
	}
	
	def "parse method def with multi var body"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""def foo(){
					var asdf = 1
					var asdf2 = 1 + 2
				}
			""")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo (vardec asdf (literal 1)) (vardec asdf2 (+ (literal 1)(literal 2))))"
	}
}

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

import chipmunk.compiler.ast.ClassNode
import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.compiler.parser.ChipmunkParser
import spock.lang.Specification

class ChipmunkParserClassSpecification extends Specification {

	def "parse class Foobar{}"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("class Foobar{}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ClassNode node = parser.parseClassDef()
		
		then:
		node.toString() == "(class Foobar)"
	}
	
	def "parse class with a variable declaration"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("class Foobar{ var a}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ClassNode node = parser.parseClassDef()
		
		then:
		node.toString() == "(class Foobar (var_dec var (id a)))"
	}
	
	def "parse class with two variable declarations"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""
			class Foobar{
				var a
				var b
			}
			"""
			)
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ClassNode node = parser.parseClassDef()
		
		then:
		node.toString() == "(class Foobar (var_dec var (id a)) (var_dec var (id b)))"
	}
	
	def "parse class with shared and final variable declarations"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""
			class Foobar{
				shared var a
				final var b
				shared final var c
			}
			"""
			)
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ClassNode node = parser.parseClassDef()
		
		then:
		node.toString() == "(class Foobar (var_dec var (id a)) (var_dec var (id b)) (var_dec var (id c)))"
	}
	
	def "parse class with variable initializations"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""
			class Foobar {
				var a = 1
				var b = 2 + 3
			}
			"""
			)
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ClassNode node = parser.parseClassDef()
		
		then:
		node.toString() == "(class Foobar (var_dec var (id a) (literal 1)) (var_dec var (id b) (+ (literal 2) (literal 3))))"
	}
	
	def "parse class with shared and final variable initialization"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""
			class Foobar{
				shared var a = 0
				final var b = 1
				shared final var c = 1 + 2
			}
			"""
			)
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ClassNode node = parser.parseClassDef()
		
		then:
		node.toString() == "(class Foobar (var_dec var (id a) (literal 0)) (var_dec var (id b) (literal 1)) (var_dec var (id c) (+ (literal 1) (literal 2))))"
	}
}

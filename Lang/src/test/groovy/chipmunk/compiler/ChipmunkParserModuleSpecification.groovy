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

import chipmunk.compiler.ast.ModuleNode
import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.compiler.parser.ChipmunkParser
import spock.lang.Specification

class ChipmunkParserModuleSpecification extends Specification {

	def "parse empty module def"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("module foobar")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		ModuleNode node = parser.parseModule();
		
		then:
		node.toString() == "(module foobar)"
	}
	
	def "parse module def with var defs"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""module foobar
			   
			   var asdf
			   var asdf2 = 1 + 2
			"""
			)
		def parser = new ChipmunkParser(tokens)
		
		when:
		ModuleNode node = parser.parseModule();
		
		then:
		node.toString() == "(module foobar (var_dec var (id asdf)) (var_dec var (id asdf2) (+ (literal 1) (literal 2))))"
	}
}

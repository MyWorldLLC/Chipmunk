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

import chipmunk.compiler.ast.AstNode
import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.compiler.parser.ChipmunkParser
import spock.lang.Specification

class ChipmunkParserExpressionSpecification extends Specification {
	
	def "parse 1"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(literal 1)"
	}
	
	def "parse foo"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(id foo)"
	}
	
	def "parse 1 + 2"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1 + 2")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(+ (literal 1)(literal 2))"
	}
	
	def "parse 1 + 2 * 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1 + 2 * 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(+ (literal 1)(* (literal 2)(literal 3)))"
	}
	
	def "parse 1 * 2 + 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1 * 2 + 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(+ (* (literal 1)(literal 2))(literal 3))"
	}
	
	def "parse (1 + 2) * 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("(1 + 2) * 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(* (+ (literal 1)(literal 2))(literal 3))"
	}
	
	def "parse 1 ** 2 ** 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1 ** 2 ** 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(** (literal 1)(** (literal 2)(literal 3)))"
	}
	
	def "parse (1 ** 2) ** 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("(1 ** 2) ** 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(** (** (literal 1)(literal 2))(literal 3))"
	}
	
	def "parse foo(true)"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo(true)")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(( (id foo)(literal true))"
	}
	
	def "parse foo(true, false)"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo(true, false)")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(( (id foo)(literal true)(literal false))"
	}
	
	def "parse 1 + foo(true, false)"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1 + foo(true, false)")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(+ (literal 1)(( (id foo)(literal true)(literal false)))"
	}
	
	def "parse foo.bar[0]"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo.bar[0]")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "([ (. (id foo)(id bar))(literal 0))"
	}
	
	def "parse foo.bar[0] = 5"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo.bar[0] = 5")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(= ([ (. (id foo)(id bar))(literal 0))(literal 5))"
	}
	
	def "parse a = b = 5"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("a = b = 5")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(= (id a)(= (id b)(literal 5)))"
	}
	
	def "parse a = 2**3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("a = 2**3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(= (id a)(** (literal 2)(literal 3)))"
	}
	
	def "parse a++"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("a++")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(++ (id a))"
	}
	
	def "parse ++a"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("++a")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(++ (id a))"
	}
	
	def "parse ++!a"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("++!a")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(++ (! (id a)))"
	}
	
	def "parse ++!a--"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("++!a--")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(++ (! (-- (id a))))"
	}
	
	def "parse !foo.bar()"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("!foo.bar()")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(! (( (. (id foo)(id bar))))"
	}
	
	def "parse []"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("[]")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(list )"
	}
	
	def "parse {}"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("{}")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(map )"
	}
	
	def "parse [a]"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("[a]")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(list (id a))"
	}
	
	def "parse [a, b, c]"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("[a, b, c]")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(list (id a)(id b)(id c))"
	}
	
	def "parse {a:b}"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("{a:b}")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(map (keyvalue (id a)(id b)))"
	}
	
	def "parse {a:b, b: c}"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("{a:b, b: c}")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(map (keyvalue (id a)(id b))(keyvalue (id b)(id c)))"
	}

}

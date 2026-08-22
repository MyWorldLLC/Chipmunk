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

class ChipmunkParserClassSpecification extends Specification {

	def "parse class Foobar{}"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("class Foobar{}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseClassDef()
		
		then:
		node.toString() == "(class Foobar <null> )"
	}
	
	def "parse class with a variable declaration"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("class Foobar{ var a}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		AstNode node = parser.parseClassDef()
		
		then:
		node.toString() == """(class Foobar <null>  
						  	  |  (var_dec a <null>  
   					          |    (id a <null> )
						  	  |  )
						      |)""".stripMargin()
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
		AstNode node = parser.parseClassDef()
		
		then:
		node.toString() == """(class Foobar <null>  
						  	  |  (var_dec a <null>  
   					          |    (id a <null> )
						  	  |  ) 
						  	  |  (var_dec b <null>  
   					          |    (id b <null> )
						  	  |  )
						      |)""".stripMargin()
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
		AstNode node = parser.parseClassDef()
		
		then:
		node.toString() == """(class Foobar <null>  
  							  |  (var_dec a <null>  
                              |    (id a <null> )
                              |  ) 
                              |  (var_dec b <null>  
                              |    (id b <null> )
                              |  ) 
                              |  (var_dec c <null>  
                              |    (id c <null> )
                              |  )
                              |)""".stripMargin()
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
		AstNode node = parser.parseClassDef()
		
		then:
		node.toString() == """(class Foobar <null>  
  							  |  (var_dec a <null>  
    						  |    (id a <null> ) 
    						  |    (literal 1 <Int> )
  							  |  ) 
  							  |  (var_dec b <null>  
    						  |    (id b <null> ) 
    						  |    (operator + <null>  
      						  |      (literal 2 <Int> ) 
      						  |      (literal 3 <Int> )
                              |    )
                              |  )
							  |)""".stripMargin()
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
		AstNode node = parser.parseClassDef()
		
		then:
		node.toString() == """(class Foobar <null>  
  							  |  (var_dec a <null>  
    						  |    (id a <null> ) 
    						  |    (literal 0 <Int> )
  							  |  ) 
  							  |  (var_dec b <null>  
    						  |    (id b <null> ) 
    						  |    (literal 1 <Int> )
    						  |  ) 
    						  |  (var_dec c <null>  
    						  |    (id c <null> ) 
    						  |    (operator + <null>  
      						  |      (literal 1 <Int> ) 
      						  |      (literal 2 <Int> )
                              |    )
                              |  )
							  |)""".stripMargin()
	}
}

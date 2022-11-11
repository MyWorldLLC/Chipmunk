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


import chipmunk.compiler.lexer.ChipmunkLexer
import chipmunk.compiler.lexer.TokenType
import spock.lang.Specification

class ChipmunkLexerSpecification extends Specification {
	
	def "Tokenize 3 + 4"(){
		
		setup:
        ChipmunkLexer lexer = new ChipmunkLexer()

		when:
		def tokens = lexer.lex("3 + 4")
		
		then:
			notThrown(SyntaxError)
			tokens.get().text() == "3"
			tokens.get().text() == "+"
			tokens.get().text() == "4"
			tokens.get().type() == TokenType.EOF
	}
	
	def "Tokenize 3 + 4 and verify types"(){
		setup:
		ChipmunkLexer lexer = new ChipmunkLexer()

		when:
		def tokens = lexer.lex("3 + 4")
		
		then:
		notThrown(SyntaxError)
		tokens.get().type() == TokenType.INTLITERAL
		tokens.get().type() == TokenType.PLUS
		tokens.get().type() == TokenType.INTLITERAL
		tokens.get().type() == TokenType.EOF
	}
	
	def "Tokenize some floats"(String src, String expect){
		setup:
		ChipmunkLexer lexer = new ChipmunkLexer()
		def token = lexer.lex(src).get()

		expect:
		token.text() == expect
		token.type() == TokenType.FLOATLITERAL

		where:
		src       || expect
		"3.0"     || "3.0"
		"4.3"     || "4.3"
		"5.6789"  || "5.6789"
		"2.5e10"  || "2.5e10"
		"2.5e-10" || "2.5e-10"
		"2.5E10"  || "2.5E10"
		"2.5E-10" || "2.5E-10"

	}
	
	def "Tokenize a comment"(){
		setup:
		def lexer = new ChipmunkLexer()
		
		when:
		def tokens = lexer.lex("3 # This is a comment.\n4")
		
		then:
		notThrown(SyntaxError)
		
		def tok = tokens.get()
		tok.text() == "3"
		tok.type() == TokenType.INTLITERAL
		
		def tok1 = tokens.get()
		tok1.text() == "# This is a comment."
		tok1.type() == TokenType.COMMENT
		
		def tok2 = tokens.get()
		tok2.text() == "\n"
		tok2.type() == TokenType.NEWLINE
		
		def tok3 = tokens.get()
		tok3.text() == "4"
		tok3.type() == TokenType.INTLITERAL
	}
	
	def "Tokenize empty module def"(){
		setup:
		def lexer = new ChipmunkLexer()
		
		when:
		def tokens = lexer.lex("module foobar")
		
		then:
		notThrown(SyntaxError)
		tokens.get().type() == TokenType.MODULE
		tokens.get().type() == TokenType.IDENTIFIER
		tokens.get().type() == TokenType.EOF
	}
	
	def "Tokenize oct literal 0o12"(){
		setup:
		def lexer = new ChipmunkLexer()
		
		when:
		def tokens = lexer.lex("0o12")
		def token = tokens.get()
		
		then:
		token.type() == TokenType.OCTLITERAL
		token.text() == "0o12"
	}
	
	def "Tokenize and verify line and column numbers"(){
		setup:
		def lexer = new ChipmunkLexer()
		
		when:
		def tokens = lexer.lex("""foo
			 | asdf
			 |	1""".stripMargin())
		
		def token1 = tokens.get()
		tokens.get()
		def token2 = tokens.get()
		tokens.get()
		def token3 = tokens.get()
		
		then:
		token1.line() == 1
		token1.column() == 1
		
		token2.line() == 2
		token2.column() == 2
		
		token3.line() == 3
		token3.column() == 5
	}

	def "Tokenize some identifiers"(){
		setup:
		def lexer = new ChipmunkLexer()

		when:
		def tokens = lexer.lex("foo foo2 _foo3 _foo_4_")

		def token1 = tokens.get()
		def token2 = tokens.get()
		def token3 = tokens.get()
		def token4 = tokens.get()

		then:
		token1.text() == "foo"
		token1.type() == TokenType.IDENTIFIER
		token2.text() == "foo2"
		token2.type() == TokenType.IDENTIFIER
		token3.text() == "_foo3"
		token3.type() == TokenType.IDENTIFIER
		token4.text() == "_foo_4_"
		token4.type() == TokenType.IDENTIFIER
	}

}

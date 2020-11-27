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
import chipmunk.compiler.lexer.Token
import spock.lang.Specification

class ChipmunkLexerSpecification extends Specification {
	
	def "Tokenize 3 + 4"(){
		
		setup:
        ChipmunkLexer lexer = new ChipmunkLexer()

		when:
		def tokens = lexer.lex("3 + 4")
		
		then:
			notThrown(SyntaxError)
			tokens.get().getText() == "3"
			tokens.get().getText() == "+"
			tokens.get().getText() == "4"
			tokens.get().getType() == Token.Type.EOF
	}
	
	def "Tokenize 3 + 4 and verify types"(){
		setup:
		ChipmunkLexer lexer = new ChipmunkLexer()

		when:
		def tokens = lexer.lex("3 + 4")
		
		then:
		notThrown(SyntaxError)
		tokens.get().getType() == Token.Type.INTLITERAL
		tokens.get().getType() == Token.Type.PLUS
		tokens.get().getType() == Token.Type.INTLITERAL
		tokens.get().getType() == Token.Type.EOF
	}
	
	def "Tokenize some floats"(){
		setup:
		ChipmunkLexer lexer = new ChipmunkLexer()
		
		when:
		def tokens = lexer.lex("3.0 4.3 5.6789 -4.3 -2.5e10 -2.5E10 -2.5E-10")
		
		then:
		notThrown(SyntaxError)
		
		def tok1 = tokens.get()
		tok1.getText() == "3.0"
		tok1.getType() == Token.Type.FLOATLITERAL
		
		def tok2 = tokens.get()
		tok2.getText() == "4.3"
		tok2.getType() == Token.Type.FLOATLITERAL
		
		def tok3 = tokens.get()
		tok3.getText() == "5.6789"
		tok3.getType() == Token.Type.FLOATLITERAL
		
		def tok4 = tokens.get()
		tok4.getText() == "-4.3"
		tok4.getType() == Token.Type.FLOATLITERAL
		
		def tok5 = tokens.get()
		tok5.getText() == "-2.5e10"
		tok5.getType() == Token.Type.FLOATLITERAL
		
		def tok6 = tokens.get()
		tok6.getText() == "-2.5E10"
		tok6.getType() == Token.Type.FLOATLITERAL
		
		def tok7 = tokens.get()
		tok7.getText() == "-2.5E-10"
		tok7.getType() == Token.Type.FLOATLITERAL
	}
	
	def "Tokenize a comment"(){
		setup:
		def lexer = new ChipmunkLexer()
		
		when:
		def tokens = lexer.lex("3 # This is a comment.\n4")
		
		then:
		notThrown(SyntaxError)
		
		def tok = tokens.get()
		tok.getText() == "3"
		tok.getType() == Token.Type.INTLITERAL
		
		def tok1 = tokens.get()
		tok1.getText() == "# This is a comment."
		tok1.getType() == Token.Type.COMMENT
		
		def tok2 = tokens.get()
		tok2.getText() == "\n"
		tok2.getType() == Token.Type.NEWLINE
		
		def tok3 = tokens.get()
		tok3.getText() == "4"
		tok3.getType() == Token.Type.INTLITERAL
	}
	
	def "Tokenize empty module def"(){
		setup:
		def lexer = new ChipmunkLexer()
		
		when:
		def tokens = lexer.lex("module foobar")
		
		then:
		notThrown(SyntaxError)
		tokens.get().getType() == Token.Type.MODULE
		tokens.get().getType() == Token.Type.IDENTIFIER
		tokens.get().getType() == Token.Type.EOF
	}
	
	def "Tokenize oct literal 0o12"(){
		setup:
		def lexer = new ChipmunkLexer()
		
		when:
		def tokens = lexer.lex("0o12")
		def token = tokens.get()
		
		then:
		token.getType() == Token.Type.OCTLITERAL
		token.getText() == "0o12"
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
		token1.getLine() == 1
		token1.getColumn() == 1
		
		token2.getLine() == 2
		token2.getColumn() == 2
		
		token3.getLine() == 3
		token3.getColumn() == 5
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
		token1.getText() == "foo"
		token1.getType() == Token.Type.IDENTIFIER
		token2.getText() == "foo2"
		token2.getType() == Token.Type.IDENTIFIER
		token3.getText() == "_foo3"
		token3.getType() == Token.Type.IDENTIFIER
		token4.getText() == "_foo_4_"
		token4.getType() == Token.Type.IDENTIFIER
	}

}

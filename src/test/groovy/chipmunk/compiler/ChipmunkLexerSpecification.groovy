package chipmunk.compiler

import chipmunk.compiler.Token.Type
import spock.lang.Specification

class ChipmunkLexerSpecification extends Specification {
	
	def "Tokenize 3 + 4"(){
		
		setup:
		ChipmunkLexer lexer = new ChipmunkLexer()

		when:
		def tokens = lexer.lex("3 + 4")
		
		then:
			notThrown(SyntaxErrorChipmunk)
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
		notThrown(SyntaxErrorChipmunk)
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
		notThrown(SyntaxErrorChipmunk)
		
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
		notThrown(SyntaxErrorChipmunk)
		
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

}

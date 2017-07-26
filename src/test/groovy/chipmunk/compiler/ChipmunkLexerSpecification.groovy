package chipmunk.compiler

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

}

package chipmunk.compiler

import chipmunk.compiler.ast.AstNode
import spock.lang.Specification

class ChipmunkParserSpecification extends Specification {
	
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

}

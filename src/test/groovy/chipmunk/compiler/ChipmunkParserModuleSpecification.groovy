package chipmunk.compiler

import chipmunk.compiler.ast.ModuleNode
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
		node.toString() == "(module foobar (vardec asdf) (vardec asdf2 (+ (literal 1)(literal 2))))"
	}
}

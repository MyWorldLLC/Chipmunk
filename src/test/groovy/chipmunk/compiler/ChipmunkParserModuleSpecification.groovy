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
	
	
}

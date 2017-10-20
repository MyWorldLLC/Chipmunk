package chipmunk.compiler

import chipmunk.compiler.ast.ImportNode
import spock.lang.Specification

class ChipmunkParserImportSpecification extends Specification {

	def "parse import foobar"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foobar")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import foobar)"
	}
	
	def "parse import foo.bar"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import bar from foo)"
	}
	
	def "parse import foo.bar.baz"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar.baz")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import baz from foo.bar)"
	}
	
	def "parse import foo.bar.*"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("import foo.bar.*")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		ImportNode node = parser.parseImport()
		
		then:
		node.toString() == "(import * from foo.bar)"
	}
}

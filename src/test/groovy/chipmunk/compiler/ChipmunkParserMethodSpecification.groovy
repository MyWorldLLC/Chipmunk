package chipmunk.compiler

import chipmunk.compiler.ast.MethodNode
import spock.lang.Specification

class ChipmunkParserMethodSpecification extends Specification {

	def "parse def foo(){}"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("def foo(){}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo)"
	}
	
	def "parse def foo(arg1){}"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("def foo(arg1){}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo (vardec arg1))"
	}
	
	def "parse def foo(arg1, arg2){}"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex("def foo(arg1, arg2){}")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo (vardec arg1) (vardec arg2))"
	}
	
	def "parse method def with single var body"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""def foo(){
					var asdf = 1
				}
			""")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo (vardec asdf (literal 1)))"
	}
	
	def "parse method def with multi var body"(){
		setup:
		def lexer = new ChipmunkLexer()
		def tokens = lexer.lex(
			"""def foo(){
					var asdf = 1
					var asdf2 = 1 + 2
				}
			""")
		
		when:
		ChipmunkParser parser = new ChipmunkParser(tokens)
		MethodNode node = parser.parseMethodDef()
		
		then:
		node.toString() == "(method foo (vardec asdf (literal 1)) (vardec asdf2 (+ (literal 1)(literal 2))))"
	}
}

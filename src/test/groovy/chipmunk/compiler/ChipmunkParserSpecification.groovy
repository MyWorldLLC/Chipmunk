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
	
	def "parse 1 * 2 + 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1 * 2 + 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(+ (* (literal 1)(literal 2))(literal 3))"
	}
	
	def "parse (1 + 2) * 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("(1 + 2) * 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(* (+ (literal 1)(literal 2))(literal 3))"
	}
	
	def "parse 1 ** 2 ** 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1 ** 2 ** 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(** (literal 1)(** (literal 2)(literal 3)))"
	}
	
	def "parse (1 ** 2) ** 3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("(1 ** 2) ** 3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(** (** (literal 1)(literal 2))(literal 3))"
	}
	
	def "parse foo(true)"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo(true)")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(( (id foo)(literal true))"
	}
	
	def "parse foo(true, false)"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo(true, false)")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(( (id foo)(literal true)(literal false))"
	}
	
	def "parse 1 + foo(true, false)"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("1 + foo(true, false)")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(+ (literal 1)(( (id foo)(literal true)(literal false)))"
	}
	
	def "parse foo.bar[0]"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo.bar[0]")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "([ (. (id foo)(id bar))(literal 0))"
	}
	
	def "parse foo.bar[0] = 5"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("foo.bar[0] = 5")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(= ([ (. (id foo)(id bar))(literal 0))(literal 5))"
	}
	
	def "parse a = b = 5"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("a = b = 5")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(= (id a)(= (id b)(literal 5)))"
	}
	
	def "parse a = 2**3"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("a = 2**3")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(= (id a)(** (literal 2)(literal 3)))"
	}
	
	def "parse a++"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("a++")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(++ (id a))"
	}
	
	def "parse ++a"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("++a")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(++ (id a))"
	}
	
	def "parse ++!a"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("++!a")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(++ (! (id a)))"
	}
	
	def "parse ++!a--"(){
		setup:
		def lexer = new ChipmunkLexer()
		lexer.lex("++!a--")
		def parser = new ChipmunkParser(lexer.getLastTokens())
		
		when:
		AstNode ast = parser.parseExpression()
		
		then:
		ast.toString() == "(++ (! (-- (id a))))"
	}
	
	

}

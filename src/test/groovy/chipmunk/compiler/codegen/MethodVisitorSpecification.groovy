package chipmunk.compiler.codegen

import chipmunk.ChipmunkContext
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.lang.CInt
import chipmunk.modules.lang.CMethod
import chipmunk.modules.lang.Null
import spock.lang.Specification

class MethodVisitorSpecification extends Specification {
	
	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	List constantPool = []
	MethodVisitor visitor = new MethodVisitor(constantPool)
	
	def "Parse, generate, and run empty method def"(){
		expect:
		parseAndCall("def method(){}") instanceof Null
	}
	
	def "Parse, generate, and run return expression method def"(){
		when:
		def result = parseAndCall("""
			def method(){
				return 1 + 2
			}
		""")
		
		then:
		result instanceof CInt
		result.getValue() == 3
	}
	
	def "Parse, generate, and run method def with var dec and return"(){
		when:
		def result = parseAndCall("""
			def method(){
				var foobar = 1 + 2
				return foobar
		}""")
		
		then:
		result instanceof CInt
		result.getValue() == 3
	}
	
	def parseAndCall(String expression){
		parser = new ChipmunkParser(lexer.lex(expression))
		
		AstNode root = parser.parseMethodDef()
		root.visit(visitor)

		CMethod method = visitor.getMethod()

		return method.__call__(new ChipmunkContext(), 0, false)
	}

}

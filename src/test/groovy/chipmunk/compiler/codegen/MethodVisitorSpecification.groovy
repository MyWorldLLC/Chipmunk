package chipmunk.compiler.codegen

import chipmunk.ChipmunkContext
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.lang.CInt
import chipmunk.modules.lang.CMethod
import chipmunk.modules.lang.CNull
import spock.lang.Specification

class MethodVisitorSpecification extends Specification {
	
	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	List constantPool = []
	MethodVisitor visitor = new MethodVisitor(constantPool)
	
	def "Parse, generate, and run empty method def"(){
		expect:
		parseAndCall("def method(){}") instanceof CNull
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
	
	def "Parse, generate, and run method def with undefined var dec and return"(){
		when:
		def result = parseAndCall("""
			def method(){
				var foobar
				return foobar
		}""")
		
		then:
		result instanceof CNull
	}
	
	def "Parse, generate, and run method def with multiple var decs and return"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 1 + 1
				var v2 = 2
				return v1 + v2
		}""")
		
		then:
		result instanceof CInt
		result.getValue() == 4
	}
	
	def "Parse, generate, and run method def with local variable assignment"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 1 + 1
				var v2 = v1
				v2 = v1 + 3
				return v2
		}""")
		
		then:
		result instanceof CInt
		result.getValue() == 5
	}
	
	def "Parse, generate, and run method def with multiple local variable assignments"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1
				var v2
				var v3 = 2 + 3
				v1 = 1
				v2 = v1 + 3
				v3 = 1
				return v1 + v2 + v3
		}""")
		
		then:
		result instanceof CInt
		result.getValue() == 6
	}
	
	def parseAndCall(String expression){
		parser = new ChipmunkParser(lexer.lex(expression))
		
		AstNode root = parser.parseMethodDef()
		root.visit(new SymbolTableBuilderVisitor())
		root.visit(visitor)

		CMethod method = visitor.getMethod()

		return method.__call__(new ChipmunkContext(), 0, false)
	}

}

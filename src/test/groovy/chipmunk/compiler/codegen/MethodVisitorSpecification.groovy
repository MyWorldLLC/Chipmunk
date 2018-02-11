package chipmunk.compiler.codegen

import chipmunk.ChipmunkVM
import chipmunk.ChipmunkDisassembler
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.reflectiveruntime.CBoolean
import chipmunk.modules.reflectiveruntime.CInteger
import chipmunk.modules.reflectiveruntime.CMethod
import chipmunk.modules.reflectiveruntime.CNull
import spock.lang.Specification

class MethodVisitorSpecification extends Specification {
	
	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	List constantPool = []
	ChipmunkVM context = new ChipmunkVM()
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
		result instanceof CInteger
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
		result instanceof CInteger
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
		result instanceof CInteger
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
		result instanceof CInteger
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
		result instanceof CInteger
		result.getValue() == 6
	}
	
	def "Basic if - return in if"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 123
				if(v1 == 123){
					return true
				}
			}
			""")
			
		then:
		result instanceof CBoolean
		result.getValue() == true
	}
	
	def "Basic if - return after if"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 123
				if(v1 != 123){
					return false
				}
				return true
			}
			""")
			
		then:
		result instanceof CBoolean
		result.getValue() == true
	}
	
	def "Multibranch if"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 123
				if(v1 == 123){
					return 1
				}else if(v1 == 124){
					return 2
				}else{
					return 3
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 1
	}
	
	def "Multibranch if - return in second branch"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 124
				if(v1 == 123){
					return 1
				}else if(v1 == 124){
					return 2
				}else{
					return 3
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 2
	}
	
	def "Multibranch if - return in else"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 125
				if(v1 == 123){
					return 1
				}else if(v1 == 124){
					return 2
				}else{
					return 3
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 3
	}
	
	def "Multibranch if - return after else"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 125
				if(v1 == 123){
					return 1
				}else if(v1 == 124){
					return 2
				}else if(v1 == 126){
					return 3
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 125
	}
	
	def "While loop - no iterations"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				while(v1 == 5){
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 0
	}
	
	def "While loop - 5 iterations"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				while(v1 < 5){
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 5
	}
	
	def "For loop over range - 5 iterations"(){
		when:
		def result = parseAndCall("""
			def forMethod(){
				var v1 = 0
				for(i in 0..<5){
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 5
	}
	
	def "For loop - break"(){
		when:
		def result = parseAndCall("""
			def forMethod(){
				var v1 = 0
				for(i in 0..<5){
					if(i == 3){
						break
					}
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 3
	}
	
	def "For loop - continue"(){
		when:
		def result = parseAndCall("""
			def forMethod(){
				var v1 = 0
				for(i in 0..<5){
					if(i == 3){
						continue
					}else{
						v1 = v1 + 1
					}
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 4
	}
	
	def "While loop - break"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				while(v1 < 5){
					v1 = v1 + 1
					if(v1 == 3){
						break
					}
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 3
	}
	
	def "While loop - continue"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = 0
				while(v1 < 5){
					if(v1 == 3){
						v1 = v1 + 2
						continue
					}
					v1 = v1 + 1
				}
				return v1
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 5
	}
	
	def "Lambda call"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = def(){return 1}
				return v1()
			}
			""")
			
		then:
		result instanceof CInteger
		result.getValue() == 1
	}
	
	
	def "Lambda call - one parameter"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = def(a){return a}
				return v1(1)
			}
			""", "")
			
		then:
		result instanceof CInteger
		result.getValue() == 1
	}
	
	def "Lambda call - two parameters"(){
		when:
		def result = parseAndCall("""
			def method(){
				var v1 = def(a, b){return a + b}
				return v1(1, 2)
			}
			""", "")
			
		then:
		result instanceof CInteger
		result.getValue() == 3
	}
	
	def "Count to a million"(){
		when:
		def result = parseAndCall("""
			def countToAMillion(){
				var x = 0
				while(x < 1000000){
					x = x + 1
				}
				return x 
			}
		""", "Count to a million")
		
		then:
		result instanceof CInteger
		result.getValue() == 1000000
	}
	
	def parseAndCall(String expression, String test = ""){
		
		parser = new ChipmunkParser(lexer.lex(expression))
		AstNode root = parser.parseMethodDef()
		root.visit(new SymbolTableBuilderVisitor())
		root.visit(visitor)
		
		CMethod method = visitor.getMethod()
		
		if(test != ""){
			println()
			println("============= ${test} =============")
			println("Local Count: ${method.getLocalCount()}")
			println(ChipmunkDisassembler.disassemble(method.getCode(), method.getConstantPool()))
		}
		if(test == ""){
			return context.dispatch(method, method.getArgCount()).getObject()
		}else{
			long start = System.nanoTime()
			def result = context.dispatch(method, method.getArgCount()).getObject()
			long end = System.nanoTime()
			println("Test ${test} took ${(end - start)/1000000000} seconds to execute")
			return result
		}
		
	}

}

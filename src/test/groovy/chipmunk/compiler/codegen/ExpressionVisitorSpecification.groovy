package chipmunk.compiler.codegen

import chipmunk.ChipmunkContext
import chipmunk.compiler.ChipmunkAssembler
import chipmunk.compiler.ChipmunkLexer
import chipmunk.compiler.ChipmunkParser
import chipmunk.compiler.SymbolTable
import chipmunk.compiler.ast.AstNode
import chipmunk.modules.lang.CCode
import chipmunk.modules.lang.CInt
import chipmunk.modules.lang.CMethod
import spock.lang.Specification

class ExpressionVisitorSpecification extends Specification {

	ChipmunkLexer lexer = new ChipmunkLexer()
	ChipmunkParser parser
	ChipmunkAssembler assembler = new ChipmunkAssembler()
	ExpressionVisitor visitor = new ExpressionVisitor(assembler, new SymbolTable())
	
		def "Generate and run code for 1 + 2"(){
			when:
			def result = parseAndCall("1 + 2")
			
			then:
			result instanceof CInt
			result.getValue() == 3
		}
		
		def parseAndCall(String expression){
			parser = new ChipmunkParser(lexer.lex(expression))
			AstNode root = parser.parseExpression()
			root.visit(visitor)
			assembler._return()
			
			CMethod method = new CMethod()
			method.setCode(new CCode(assembler.getCodeSegment()))
			method.setConstantPool(assembler.getConstantPool())
			
			return method.__call__(new ChipmunkContext(), 0, false)
		}
}

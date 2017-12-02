package chipmunk.compiler.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;

public class ExpressionStatementVisitor implements AstVisitor {

	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	
	public ExpressionStatementVisitor(Codegen codegen){
		assembler = codegen.getAssembler();
		symbols = codegen.getSymbols();
	}
	
	@Override
	public void visit(AstNode node) {
		node.visit(new ExpressionVisitor(assembler, symbols));
		// evaluate expression and ignore result
		assembler.pop();
	}

}

package chipmunk.compiler.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;

public class ExpressionStatementVisitor implements AstVisitor {

	protected ChipmunkAssembler assembler;
	protected Codegen codegen;
	
	public ExpressionStatementVisitor(Codegen codegen){
		this.codegen = codegen;
		assembler = codegen.getAssembler();
	}
	
	@Override
	public void visit(AstNode node) {
		node.visit(new ExpressionVisitor(assembler, codegen.getActiveSymbols()));
		// evaluate expression and ignore result
		assembler.pop();
	}

}

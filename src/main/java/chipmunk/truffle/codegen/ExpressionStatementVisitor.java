package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;

public class ExpressionStatementVisitor implements AstVisitor {

	protected ChipmunkAssembler assembler;
	protected TruffleCodegen codegen;
	
	public ExpressionStatementVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		node.visit(new ExpressionVisitor(codegen));
		// evaluate expression and ignore result
		assembler.pop();
	}

}

package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.truffle.ast.StatementNode;

public class ExpressionStatementVisitor implements TruffleAstVisitor<StatementNode> {

	protected ChipmunkAssembler assembler;
	protected TruffleCodegen codegen;
	
	public ExpressionStatementVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public StatementNode visit(AstNode node) {
		//node.visit(new ExpressionVisitor(codegen));
		// evaluate expression and ignore result
		assembler.pop();
		return null;
	}

}

package chipmunk.truffle.codegen;

import chipmunk.compiler.ast.AstNode;
import chipmunk.truffle.ast.StatementNode;

public class BlockBodyVisitor implements TruffleAstVisitor<StatementNode> {
	
	protected TruffleCodegen codegen;
	
	public BlockBodyVisitor(TruffleCodegen codegen) {
		this.codegen = codegen;
	}

	@Override
	public StatementNode visit(AstNode node) {
		return (StatementNode) codegen.emit(node);
	}

}

package chipmunk.truffle.codegen;

import chipmunk.compiler.ast.AstNode;
import chipmunk.truffle.ast.BlockNode;
import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.StatementNode;
import chipmunk.truffle.ast.flow.WhileNode;

public class WhileVisitor implements TruffleAstVisitor<WhileNode> {

	private TruffleCodegen codegen;
	
	public WhileVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public WhileNode visit(AstNode node) {
		chipmunk.compiler.ast.WhileNode whileNode = (chipmunk.compiler.ast.WhileNode) node;
		
		codegen.enterScope(whileNode.getSymbolTable());
		
		ExpressionNode condition = new ExpressionVisitor(codegen).visit(whileNode.getChildren().get(0));
		
		codegen.enterLoop();
		
		StatementNode[] bodyNodes = new StatementNode[whileNode.getChildren().size() - 1];
		
		for(int i = 1; i < whileNode.getChildren().size(); i++) {
			bodyNodes[i - 1] = new BlockBodyVisitor(codegen).visit(whileNode.getChildren().get(i));
		}
		
		codegen.exitLoop();
		
		BlockNode block = new BlockNode(bodyNodes);
		
		codegen.exitScope();
		
		return new WhileNode(condition, block);
	}

}

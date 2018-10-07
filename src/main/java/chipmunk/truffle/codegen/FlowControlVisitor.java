package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.FlowControlNode;
import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.StatementNode;
import chipmunk.truffle.ast.flow.BreakNode;
import chipmunk.truffle.ast.flow.ContinueNode;
import chipmunk.truffle.ast.flow.ReturnNode;
import chipmunk.truffle.ast.flow.ThrowNode;

public class FlowControlVisitor implements TruffleAstVisitor<StatementNode> {
	
	protected TruffleCodegen codegen;
	protected ChipmunkAssembler assembler;
	
	public FlowControlVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}

	@Override
	public StatementNode visit(AstNode node) {
		if(node instanceof FlowControlNode){
			FlowControlNode flowNode = (FlowControlNode) node;
			
			Token token = flowNode.getControlToken();
			
			if(token.getType() == Token.Type.RETURN){
				ExpressionNode returnExp = null;
				
				if(node.hasChildren()){
					returnExp = new ExpressionVisitor(codegen).visit(node);
				}
				
				return new ReturnNode(returnExp);
			}else if(token.getType() == Token.Type.THROW){
				ExpressionNode throwNode = new ExpressionVisitor(codegen).visit(node);
				return new ThrowNode(throwNode);
			}else if(token.getType() == Token.Type.BREAK){
				if(!codegen.inLoop()){
					//throw new CompileChipmunk(String.format("Invalid break at %s: %d: Can only break when inside a loop", token.getFile(), token.getLine()));
					throw new CompileChipmunk(String.format("Invalid break at %d: Can only break when inside a loop", token.getLine()));
	
				}
				return new BreakNode();
			}else if(token.getType() == Token.Type.CONTINUE){
				if(!codegen.inLoop()){
					//throw new CompileChipmunk(String.format("Invalid continue at %s: %d: Can only continue when inside a loop", token.getFile(), token.getLine()));
					throw new CompileChipmunk(String.format("Invalid continue at %d: Can only continue when inside a loop", token.getLine()));
				}
				return new ContinueNode();
			}
		}
		return null;
	}

}

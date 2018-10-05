package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.FlowControlNode;

public class FlowControlVisitor implements AstVisitor {
	
	protected TruffleCodegen codegen;
	protected ChipmunkAssembler assembler;
	
	public FlowControlVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof FlowControlNode){
			FlowControlNode flowNode = (FlowControlNode) node;
			
			Token token = flowNode.getControlToken();
			
			if(token.getType() == Token.Type.RETURN){
				if(node.hasChildren()){
					node.visitChildren(new ExpressionVisitor(codegen));
				}else{
					assembler.pushNull();
				}
				assembler._return();
			}else if(token.getType() == Token.Type.THROW){
				node.visitChildren(new ExpressionVisitor(codegen));
				assembler._throw();
			}else if(token.getType() == Token.Type.BREAK){
				/*if(!codegen.inLoop()){
					//throw new CompileChipmunk(String.format("Invalid break at %s: %d: Can only break when inside a loop", token.getFile(), token.getLine()));
					throw new CompileChipmunk(String.format("Invalid break at %d: Can only break when inside a loop", token.getLine()));
	
				}*/
				//assembler._goto(codegen.peekClosestLoop().getEndLabel());
			}else if(token.getType() == Token.Type.CONTINUE){
				/*if(!codegen.inLoop()){
					//throw new CompileChipmunk(String.format("Invalid continue at %s: %d: Can only continue when inside a loop", token.getFile(), token.getLine()));
					throw new CompileChipmunk(String.format("Invalid continue at %d: Can only continue when inside a loop", token.getLine()));
				}*/
				//assembler._goto(codegen.peekClosestLoop().getGuardLabel());
			}
			return;
		}
	}

}

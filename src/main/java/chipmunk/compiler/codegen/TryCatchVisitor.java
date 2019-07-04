package chipmunk.compiler.codegen;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.CatchNode;
import chipmunk.compiler.ast.FinallyNode;
import chipmunk.compiler.ast.TryCatchNode;
import chipmunk.compiler.ast.TryNode;

public class TryCatchVisitor implements AstVisitor {

	protected Codegen codegen;
	protected String finallyLabel; // TODO - won't work with nested try/catch blocks
	
	public TryCatchVisitor(Codegen codegen) {
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		if(node instanceof TryCatchNode) {
			
			finallyLabel = codegen.assembler.nextLabelName();
			node.visitChildren(this);
			
		}else if(node instanceof TryNode) {
			// TODO - mark try body start
			// Assemble try body
			node.visitChildren(codegen);
			// TODO - mark try body end
		}else if(node instanceof CatchNode) {
			// TODO - assemble catch body
		}else if(node instanceof FinallyNode) {
			// TODO - assemble finally body
		}

	}

}

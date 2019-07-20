package chipmunk.compiler.codegen;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.CatchNode;
import chipmunk.compiler.ast.FinallyNode;
import chipmunk.compiler.ast.TryCatchNode;
import chipmunk.compiler.ast.TryNode;

public class TryCatchVisitor implements AstVisitor {

	protected Codegen codegen;
	
	public TryCatchVisitor(Codegen codegen) {
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		TryCatchLabels labels = codegen.pushTryCatch();
		if(node instanceof TryCatchNode) {
			
			node.visitChildren(this);
			
			// TODO - build exception tables
		}else if(node instanceof TryNode) {
			codegen.getAssembler().setLabelTarget(labels.getStartLabel());
		
			// Assemble try body
			node.visitChildren(codegen);
			
			codegen.getAssembler().setLabelTarget(labels.getEndLabel());
		}else if(node instanceof CatchNode) {
			BlockLabels catchLabels = new BlockLabels(codegen.getAssembler().nextLabelName(), codegen.getAssembler().nextLabelName());
			labels.getCatchBlocks().add(catchLabels);
			
			codegen.getAssembler().setLabelTarget(catchLabels.getStartLabel());
			
			// Assemble catch body
			node.visitChildren(codegen);
			
			codegen.getAssembler().setLabelTarget(catchLabels.getEndLabel());
		}else if(node instanceof FinallyNode) {
			BlockLabels finallyLabels = new BlockLabels(codegen.getAssembler().nextLabelName(), codegen.getAssembler().nextLabelName());
			labels.getCatchBlocks().add(finallyLabels);
			
			codegen.getAssembler().setLabelTarget(finallyLabels.getStartLabel());
			
			// Assemble finally body
			node.visitChildren(codegen);
			
			codegen.getAssembler().setLabelTarget(finallyLabels.getEndLabel());
		}

	}

}

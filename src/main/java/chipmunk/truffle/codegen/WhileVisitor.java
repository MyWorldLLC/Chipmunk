package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.WhileNode;

public class WhileVisitor implements AstVisitor {

	private ChipmunkAssembler assembler;
	private TruffleCodegen codegen;
	
	public WhileVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		if(node instanceof WhileNode){
			WhileNode loop = (WhileNode) node;
			
			LoopLabels labels = null; //codegen.pushLoop();
			
			assembler.setLabelTarget(labels.getStartLabel());
			assembler.setLabelTarget(labels.getGuardLabel());
			
			loop.getGuard().visit(new ExpressionVisitor(codegen));
			
			// if guard does not evaluate true, jump to end
			assembler._if(labels.getEndLabel());
			
			codegen.enterScope(loop.getSymbolTable());
			// generate body
			//loop.visitChildren(codegen, 1);
			codegen.exitScope();
			
			// jump to guard
			assembler._goto(labels.getGuardLabel());
			
			// set end label target
			assembler.setLabelTarget(labels.getEndLabel());
			
			//codegen.exitLoop();
		}
	}

}

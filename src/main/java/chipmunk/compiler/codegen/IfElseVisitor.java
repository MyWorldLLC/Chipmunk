package chipmunk.compiler.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.BlockNode;
import chipmunk.compiler.ast.GuardedNode;
import chipmunk.compiler.ast.IfElseNode;

public class IfElseVisitor implements AstVisitor {
	
	private ChipmunkAssembler assembler;
	private SymbolTable symbols;
	private Codegen codegen;
	
	private String endLabel;
	
	public IfElseVisitor(Codegen codegen){
		this.codegen = codegen;
		assembler = codegen.getAssembler();
		symbols = codegen.getActiveSymbols();
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof IfElseNode){
			IfElseNode ifElse = (IfElseNode) node;
			endLabel = assembler.nextLabelName();
			
			ifElse.visitChildren(this);
			
			// label the end of the if/else
			assembler.setLabelTarget(endLabel);
		}else if(node instanceof GuardedNode){
			GuardedNode ifBranch = (GuardedNode) node;
			ifBranch.getGuard().visit(new ExpressionVisitor(codegen, symbols));
			
			String endOfIf = assembler.nextLabelName();
			// go to end of this node's body if the if does not evaluate true
			assembler._if(endOfIf);
			
			// generate code for the children, skipping the guard statement
			ifBranch.visitChildren(codegen, 1);
			
			// go to the end of the entire if/else if body executes
			assembler._goto(endLabel);
			// mark end of the if block
			assembler.setLabelTarget(endOfIf);
			
		}else if(node instanceof BlockNode){
			BlockNode elseBranch = (BlockNode) node;
			elseBranch.visitChildren(codegen);
		}
	}

}

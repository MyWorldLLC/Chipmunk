package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.BlockNode;
import chipmunk.compiler.ast.GuardedNode;
import chipmunk.truffle.ast.flow.IfElseNode;

public class IfElseVisitor implements TruffleAstVisitor<IfElseNode> {
	
	private ChipmunkAssembler assembler;
	private SymbolTable symbols;
	private TruffleCodegen codegen;
	
	private String endLabel;
	
	public IfElseVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
		symbols = codegen.getActiveSymbols();
	}

	@Override
	public IfElseNode visit(AstNode node) {
		if(node instanceof chipmunk.compiler.ast.IfElseNode){
			chipmunk.compiler.ast.IfElseNode ifElse = (chipmunk.compiler.ast.IfElseNode) node;
			endLabel = assembler.nextLabelName();
			
			this.visit(ifElse);
			
			// label the end of the if/else
			assembler.setLabelTarget(endLabel);
		}else if(node instanceof GuardedNode){
			GuardedNode ifBranch = (GuardedNode) node;
			//ifBranch.getGuard().visit(new ExpressionVisitor(codegen));
			
			String endOfIf = assembler.nextLabelName();
			// go to end of this node's body if the if does not evaluate true
			assembler._if(endOfIf);
			
			// generate code for the children, skipping the guard statement
			//ifBranch.visitChildren(codegen, 1);
			
			// go to the end of the entire if/else if body executes
			assembler._goto(endLabel);
			// mark end of the if block
			assembler.setLabelTarget(endOfIf);
			
		}else if(node instanceof BlockNode){
			BlockNode elseBranch = (BlockNode) node;
			//elseBranch.visitChildren(codegen);
		}
		return null; // TODO
	}

}

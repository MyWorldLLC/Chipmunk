package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.truffle.ast.flow.ForNode;

public class ForVisitor implements TruffleAstVisitor<ForNode> {

	private ChipmunkAssembler assembler;
	private TruffleCodegen codegen;
	
	public ForVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public ForNode visit(AstNode node) {
		if(node instanceof chipmunk.compiler.ast.ForNode){
			chipmunk.compiler.ast.ForNode loop = (chipmunk.compiler.ast.ForNode) node;
			
			SymbolTable symbols = loop.getSymbolTable();
			LoopLabels labels = null;// codegen.pushLoop();
			
			assembler.setLabelTarget(labels.getStartLabel());
			
			VarDecNode id = loop.getID();
			id.getSymbol().setFinal(true);
			
			// visit iterator expression and push the iterator
			//loop.getIter().visit(new ExpressionVisitor(codegen));
			assembler.iter();
			
			// the "next" bytecode operates as the guard in the for loop
			assembler.setLabelTarget(labels.getGuardLabel());
			assembler.next(labels.getEndLabel());
			
			// set the next value in the iterator as a local variable
			assembler.setLocal(symbols.getLocalIndex(id.getVarName()));
			assembler.pop();

			// generate body
			codegen.enterScope(symbols);
			//loop.visitChildren(codegen, 2);
			codegen.exitScope();
			
			// jump to iterator
			assembler._goto(labels.getGuardLabel());
			
			// set end label target
			assembler.setLabelTarget(labels.getEndLabel());
			
			//codegen.exitLoop();
		}

		return null; // TODO
	}

}

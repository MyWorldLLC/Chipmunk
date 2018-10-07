package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ForNode;
import chipmunk.compiler.ast.VarDecNode;

public class ForVisitor implements AstVisitor {

	private ChipmunkAssembler assembler;
	private TruffleCodegen codegen;
	
	public ForVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		if(node instanceof ForNode){
			ForNode loop = (ForNode) node;
			
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
	}

}

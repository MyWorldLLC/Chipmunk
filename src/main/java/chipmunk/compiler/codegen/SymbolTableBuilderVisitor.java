package chipmunk.compiler.codegen;

import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.BlockNode;
import chipmunk.compiler.ast.SymbolNode;

public class SymbolTableBuilderVisitor implements AstVisitor {
	
	protected SymbolTable currentScope;

	@Override
	public boolean preVisit(AstNode node) {
		
		if(node instanceof SymbolNode){
			currentScope.setSymbol(((SymbolNode) node).getSymbol());
		}
		
		if(node instanceof BlockNode){
			BlockNode block = (BlockNode) node;
			block.getSymbolTable().setParent(currentScope);
			currentScope = block.getSymbolTable();
		}
		return true;
	}

	@Override
	public void postVisit(AstNode node) {
		if(node instanceof BlockNode){
			currentScope = currentScope.getParent();
		}
	}

}

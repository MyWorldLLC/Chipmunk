package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.BlockNode;
import chipmunk.compiler.ast.CatchNode;
import chipmunk.compiler.ast.ImportNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.SymbolNode;

public class SymbolTableBuilderVisitor implements AstVisitor {
	
	protected SymbolTable currentScope;

	@Override
	public void visit(AstNode node) {
		
		if(node instanceof SymbolNode){
			if(currentScope != null){
				Symbol symbol = ((SymbolNode) node).getSymbol();
				// only set non-empty symbols - otherwise superfluous local slots are created for
				// anonymous methods/classes
				if(!symbol.getName().equals("")) {
					currentScope.setSymbol(symbol);
				}
			}
		}
		
		if(node instanceof BlockNode){
			BlockNode block = (BlockNode) node;
			SymbolTable blockTable = block.getSymbolTable();
			blockTable.setParent(currentScope);
			
			if(node instanceof MethodNode){
				blockTable.setSymbol(new Symbol("self", true));
			}

			currentScope = blockTable;
			
		}
		
		if(node instanceof ImportNode){
			ImportNode importNode = (ImportNode) node;
			
			List<String> symbols = new ArrayList<String>();
			
			if(importNode.hasAliases()){
				symbols = importNode.getAliases();
			}else{
				symbols = importNode.getSymbols();
			}
			
			for(String symbol : symbols){
				currentScope.setSymbol(new Symbol(symbol, true));
			}
		}
		
		node.visitChildren(this);
		
		if(currentScope != null && node instanceof BlockNode){
			currentScope = currentScope.getParent();
		}
	}

}

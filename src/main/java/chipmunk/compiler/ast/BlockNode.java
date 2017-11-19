package chipmunk.compiler.ast;

import chipmunk.compiler.SymbolTable;

public class BlockNode extends AstNode {
	
	protected SymbolTable symTab;
	
	public BlockNode(){
		super();
		symTab = new SymbolTable();
		symTab.setNode(this);
	}
	
	public BlockNode(SymbolTable.Scope scope){
		super();
		symTab = new SymbolTable(scope);
	}
	
	public BlockNode(AstNode... children){
		super(children);
		symTab = new SymbolTable();
	}
	
	public BlockNode(SymbolTable.Scope scope, AstNode... children){
		super(children);
		symTab = new SymbolTable(scope);
	}
	
	public SymbolTable getSymbolTable(){
		return symTab;
	}
	
	public void setParentSymbolTable(BlockNode node){
		symTab.setParent(node.getSymbolTable());
	}
	
	public void addToBody(AstNode node){
		super.addChild(node);
	}
	
	public void addToBody(AstNode... nodes){
		super.addChildren(nodes);
	}

}

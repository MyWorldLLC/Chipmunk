package chipmunk.compiler.ast;

import chipmunk.compiler.SymbolTable;

public class ScopedNode extends AstNode {
	
	protected SymbolTable symTab;
	
	public ScopedNode(){
		super();
		symTab = new SymbolTable();
	}
	
	public ScopedNode(AstNode... children){
		super(children);
		symTab = new SymbolTable();
	}
	
	public SymbolTable getSymbolTable(){
		return symTab;
	}

}

package chipmunk.compiler.ast;

import chipmunk.compiler.SymbolTable;

public class ScopedNode extends AstNode {
	
	protected SymbolTable symTab;
	
	public ScopedNode(){
		super();
		symTab = new SymbolTable();
	}
	
	public ScopedNode(SymbolTable.Scope scope){
		super();
		symTab = new SymbolTable(scope);
	}
	
	public ScopedNode(AstNode... children){
		super(children);
		symTab = new SymbolTable();
	}
	
	public ScopedNode(SymbolTable.Scope scope, AstNode... children){
		super(children);
		symTab = new SymbolTable(scope);
	}
	
	public SymbolTable getSymbolTable(){
		return symTab;
	}

}

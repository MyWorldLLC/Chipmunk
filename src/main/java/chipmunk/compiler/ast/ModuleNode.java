package chipmunk.compiler.ast;

import chipmunk.compiler.SymbolTable;

public class ModuleNode extends ScopedNode {

	protected String name;
	
	public ModuleNode(){
		super(SymbolTable.Scope.MODULE);
		name = "";
	}
	
	public ModuleNode(String name){
		super();
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
}

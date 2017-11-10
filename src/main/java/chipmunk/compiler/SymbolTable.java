package chipmunk.compiler;

import java.util.HashMap;
import java.util.Map;

import chipmunk.compiler.ast.BlockNode;

public class SymbolTable {
	
	public enum Scope {
		MODULE, CLASS, LOCAL
	}
	
	protected Map<String, Symbol> symbols;
	protected SymbolTable parent;
	protected Scope scope;
	protected BlockNode node;
	
	public SymbolTable(SymbolTable parent){
		this();
		this.parent = parent;
	}
	public SymbolTable(){
		symbols = new HashMap<String, Symbol>();
	}
	
	public SymbolTable(SymbolTable.Scope scope){
		this();
		this.scope = scope;
	}
	
	public void setSymbol(Symbol symbol){
		symbols.put(symbol.getName(), symbol);
	}
	
	public Symbol getSymbol(String name){
		if(!symbols.containsKey(name) && parent != null){
			return parent.getSymbol(name);
		}
		return symbols.get(name);
	}
	
	public boolean isSymbolSet(String name, boolean searchParents){
		if(searchParents && parent != null){
			if(parent.isSymbolSet(name, true)){
				return true;
			}
		}
		return symbols.containsKey(name);
	}
	
	public Scope getScope(){
		return scope;
	}
	
	public void setScope(Scope scope){
		this.scope = scope;
	}
	
	public BlockNode getNode(){
		return node;
	}
	
	public void setNode(BlockNode node){
		this.node = node;
	}
	
	public SymbolTable getParent(){
		return parent;
	}
	
	public void setParent(SymbolTable parent){
		this.parent = parent;
	}
	
}

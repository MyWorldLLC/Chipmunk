package chipmunk.compiler;

import java.util.HashMap;
import java.util.Map;

import chipmunk.compiler.ast.ScopedNode;

public class SymbolTable {
	
	public enum Scope {
		MODULE, CLASS, LOCAL
	}
	
	protected Map<String, Symbol> symbols;
	protected SymbolTable parent;
	protected Scope scope;
	protected ScopedNode node;
	
	public SymbolTable(SymbolTable parent){
		this();
		this.parent = parent;
	}
	public SymbolTable(){
		symbols = new HashMap<String, Symbol>();
	}
	
	public void setSymbol(Symbol symbol){
		symbols.put(symbol.getName(), symbol);
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
	
	public ScopedNode getNode(){
		return node;
	}
	
	public void setNode(ScopedNode node){
		this.node = node;
	}
	
}

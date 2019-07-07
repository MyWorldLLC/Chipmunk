package chipmunk.compiler;

import java.util.ArrayList;
import java.util.List;

import chipmunk.compiler.ast.BlockNode;

public class SymbolTable {
	
	public enum Scope {
		MODULE, CLASS, METHOD, LOCAL
	}
	
	protected List<Symbol> symbols;
	protected SymbolTable parent;
	protected Scope scope;
	protected int localStartIndex;
	protected int maxChildLocalCount;
	protected BlockNode node;
	
	public SymbolTable(){
		this(Scope.LOCAL);
	}
	
	public SymbolTable(SymbolTable.Scope scope){
		symbols = new ArrayList<Symbol>();
		setScope(scope);
	}
	
	public void setSymbol(Symbol symbol){
		if(!symbols.contains(symbol)){
			symbols.add(symbol);
		}
		if(parent != null){
			parent.reportChildLocalCount(symbols.size());
		}
	}
	
	public Symbol getSymbol(String name){
		Symbol symbolName = new Symbol(name);
		
		if(!symbols.contains(symbolName)){
			if(parent != null){
				return parent.getSymbol(name);
			}else{
				return null;
			}
		}
		return symbols.get(symbols.indexOf(symbolName));
	}
	
	public void clearSymbol(Symbol symbol){
		int symbolIndex = symbols.indexOf(symbol);
		
		if(symbolIndex != -1){
			symbols.remove(symbolIndex);
		}
	}
	
	public boolean isSymbolSet(String name, boolean searchParents){
		int symbolIndex = symbols.indexOf(new Symbol(name));
		
		if(symbolIndex == -1 && searchParents && parent != null){
			if(parent.isSymbolSet(name, true)){
				return true;
			}
		}
		return symbolIndex != -1 ? true : false;
	}
	
	public int getLocalIndex(String symbolName){
		return getLocalIndex(new Symbol(symbolName));
	}
	
	public int getLocalIndex(Symbol symbol){
		// TODO - support closures
		if(scope == Scope.LOCAL || scope == Scope.METHOD){
			if(symbols.contains(symbol)){
				System.out.println(symbol.getName() + ": " + (symbols.indexOf(symbol) + localStartIndex));
				return symbols.indexOf(symbol) + localStartIndex;
			}
			if(parent != null){

				System.out.println(symbol.getName() + ": " + parent.getLocalIndex(symbol));
				return parent.getLocalIndex(symbol);
			}
		}
		return -1;
	}
	
	public Scope getScope(){
		return scope;
	}
	
	public void setScope(Scope scope){
		this.scope = scope;
		if(scope == Scope.LOCAL || scope == Scope.METHOD){
			// If scope changes to local, reset local min/max counts
			// either their current values or 0 (preserves local min/max
			// if scope is local and is set to local)
			localStartIndex = Math.max(0, localStartIndex);
			maxChildLocalCount = Math.max(0, maxChildLocalCount);
		}else{
			localStartIndex = -1;
			maxChildLocalCount = -1;
		}
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
		calculateLocalStartIndex();
		if(isInnerLocal()){
			parent.reportChildLocalCount(this.getLocalMax());
		}
	}
	
	public void reportChildLocalCount(int childLocalCount){
		if(scope == Scope.LOCAL || scope == Scope.METHOD){
			maxChildLocalCount = Math.max(maxChildLocalCount, childLocalCount);
			if(isInnerLocal()){
				parent.reportChildLocalCount(getLocalMax());
			}
		}
	}
	
	public int getLocalMax(){
		return maxChildLocalCount + symbols.size();
	}
	
	public void calculateLocalStartIndex(){
		localStartIndex = 0;
		if(scope == Scope.LOCAL && isInnerLocal()){
			localStartIndex = parent.getLocalStartIndex() + parent.symbols.size();
		}
	}
	
	public int getLocalStartIndex(){
		return localStartIndex;
	}
	
	public boolean isInnerLocal(){
		if(parent != null && (parent.scope == Scope.LOCAL || parent.scope == Scope.METHOD)){
			return true;
		}
		return false;
	}
	
	public List<Symbol> getAllSymbols(){
		return symbols;
	}
	
	public int getSymbolCount(){
		return symbols.size();
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		if(parent != null){
			builder.append(parent.toString());
			builder.append("\n^");
		}
		builder.append(symbols.toString());
		return builder.toString();
	}
}

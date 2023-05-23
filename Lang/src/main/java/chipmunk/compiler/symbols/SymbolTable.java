/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.compiler.symbols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chipmunk.compiler.ast.AstNode;

public class SymbolTable {

	public static final int UNDEFINED_LOCAL_START_INDEX = -1;
	
	public enum Scope {
		MODULE, CLASS, METHOD, LOCAL
	}
	
	protected List<Symbol> symbols;
	protected String debugSymbol;
	protected SymbolTable parent;
	protected Scope scope;
	protected int maxChildLocalCount;
	protected AstNode node;
	
	public SymbolTable(){
		this(Scope.LOCAL);
	}

	public SymbolTable(SymbolTable.Scope scope){
		this(scope, null);
	}

	public SymbolTable(SymbolTable.Scope scope, AstNode node){
		symbols = new ArrayList<>();
		setScope(scope);
		setNode(node);
	}
	
	public void setSymbol(Symbol symbol){
		if(!symbols.contains(symbol)){
			symbols.add(symbol);
			symbol.setTable(this);
		}
		if(parent != null){
			parent.reportChildLocalCount(symbols.size());
		}
	}

	public boolean removeSymbol(Symbol symbol){
		return symbols.remove(symbol);
	}
	
	public Symbol getSymbol(String name){
		Symbol symbol = new Symbol(name);
		
		if(!symbols.contains(symbol)){
			if(parent != null){
				return parent.getSymbol(name);
			}else{
				return null;
			}
		}
		return symbols.get(symbols.indexOf(symbol));
	}

	public Symbol getSymbolLocal(String name){
		Symbol symbol = new Symbol(name);
		if(symbols.contains(symbol)){
			return symbols.get(symbols.indexOf(symbol));
		}
		return null;
	}

	public List<Symbol> getSymbolsUnmodifiable(){
		return Collections.unmodifiableList(symbols);
	}

	public boolean isSymbolSet(String name, boolean searchParents){
		int symbolIndex = symbols.indexOf(new Symbol(name));
		
		if(symbolIndex == -1 && searchParents && parent != null){
			if(parent.isSymbolSet(name, true)){
				return true;
			}
		}
		return symbolIndex != -1;
	}
	
	public int getLocalIndex(String symbolName){
		return getLocalIndex(new Symbol(symbolName));
	}
	
	public int getLocalIndex(Symbol symbol){
		if(scope == Scope.LOCAL || scope == Scope.METHOD){
			if(symbols.contains(symbol)){
				return symbols.indexOf(symbol) + getLocalStartIndex();
			}
			if(parent != null){
				return parent.getLocalIndex(symbol);
			}
		}
		return UNDEFINED_LOCAL_START_INDEX;
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
			maxChildLocalCount = Math.max(0, maxChildLocalCount);
		}else{
			maxChildLocalCount = -1;
		}
	}
	
	public AstNode getNode(){
		return node;
	}
	
	public void setNode(AstNode node){
		this.node = node;
	}
	
	public SymbolTable getParent(){
		return parent;
	}

	public SymbolTable getParent(Scope scope){
		var parent = getParent();
		while(parent != null && parent.getScope() != scope){
			parent = parent.getParent();
		}
		return parent;
	}

	public SymbolTable nearest(Scope scope){
		return findTable(t -> t.getScope() == scope);
	}
	
	public void setParent(SymbolTable parent){
		this.parent = parent;
		getLocalStartIndex();
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
	
	public int getLocalStartIndex(){

		if(!isMethodScope()){
			return -1;
		}

		var localStartIndex = 0;

		if(parent != null && parent.isMethodScope()){
			localStartIndex = parent.getLocalStartIndex() + parent.symbols.size();
		}

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
	
	public void setDebugSymbol(String debugSymbol) {
		this.debugSymbol = debugSymbol;
	}
	
	public String getDebugSymbol() {
		List<String> symbols = new ArrayList<>();
		
		if(parent != null) {
			
			SymbolTable debugParent = parent;
			while(debugParent != null) {
				String parentSymbol = debugParent.getDebugSymbol();
				if(parentSymbol != null) {
					if("".equals(parentSymbol)) {
						symbols.add("<anon>");
					} else {
						symbols.add(debugParent.getDebugSymbol());
					}
				}
				debugParent = debugParent.getParent();
			}
		}
		
		if(debugSymbol != null) {
			symbols.add(debugSymbol);
		}
		
		return String.join(".", symbols);
	}

	public Scope getMethodContainingScope(){
		SymbolTable symbols = this;
		while(symbols.getScope() == Scope.LOCAL){
			symbols = symbols.getParent();
		}

		// Parent is either (a) a module, (b) a class, or (c) a local scope (lambdas)
		return symbols.getParent().getScope();
	}

	public boolean isClosured(Symbol symbol){
		SymbolTable symbols = this;
		boolean crossedMethodBoundary = false;
		while(symbols.isMethodScope() && symbols != symbol.getTable()){
			if(symbols.getScope() == Scope.METHOD){
				crossedMethodBoundary = true;
			}
			symbols = symbols.getParent();
		}
		return symbols.isMethodScope() && crossedMethodBoundary;
	}

	public SymbolTable getMethodTable(){
		SymbolTable symbols = this;
		while(symbols.getScope() == Scope.LOCAL){
			symbols = symbols.getParent();
		}

		return symbols;
	}

	public boolean isSharedMethodScope(){
		SymbolTable table = getMethodTable();
		return table.getNode().getSymbol().isShared();
	}

	public SymbolTable getModuleScope(){
		SymbolTable symbols = this;
		while(symbols.getScope() != Scope.MODULE){
			symbols = symbols.getParent();
		}

		return symbols;
	}

	public List<Symbol> getUpvalueRefs(){
		return findSymbols(s -> s.getUpvalueRef() != null);
	}

	public boolean isModuleMethodScope(){
		return getMethodContainingScope() == Scope.MODULE;
	}

	public boolean isClassMethodScope(){
		return getMethodContainingScope() == Scope.CLASS;
	}

	public boolean isInnerMethodScope(){
		return getMethodContainingScope() == Scope.METHOD;
	}

	public boolean isMethodScope(){
		return scope == Scope.LOCAL || scope == Scope.METHOD;
	}

	public SymbolTable findTable(Predicate<SymbolTable> p){
		var table = this;
		while(table != null && !p.test(table)){
			table = table.getParent();
		}
		return table;
	}

	public List<Symbol> findSymbols(Predicate<Symbol> s){
		return symbols.stream().filter(s).toList();
	}

	public void sortSymbols(Comparator<Symbol> c){
		symbols.sort(c);
	}

	public int count(Predicate<Symbol> p){
		return (int) symbols.stream().filter(p).count();
	}

	public int countUpvalues(){
		return count(Symbol::isUpvalue);
	}

	public int countUpvalueRefs(){
		return count(s -> s.getUpvalueRef() != null);
	}

	public String toString(boolean pretty){
		StringBuilder builder = new StringBuilder();
		if(parent != null){
			builder.append(parent.toString(pretty));
			builder.append("\n^");
		}
		builder.append(pretty
				? symbols.stream().map(Symbol::toString).collect(Collectors.joining(",\n  ", "[\n  ", "\n]"))
				: symbols);
		return builder.toString();
	}
	@Override
	public String toString(){
		return toString(false);
	}
}

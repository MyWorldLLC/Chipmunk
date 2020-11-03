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

package chipmunk.compiler;

public class Symbol {

	public static class Import {
		protected final String module;
		protected final String aliased;

		public Import(String module){
			this(module, null);
		}

		public Import(String module, String aliased){
			this.module = module;
			this.aliased = aliased;
		}

		public String getModule() {
			return module;
		}

		public String getAliasedSymbol() {
			return aliased;
		}

		public boolean isAliased(){
			return aliased != null;
		}

	}
	
	protected boolean isShared;
	protected boolean isFinal;
	protected boolean isClosure;
	protected boolean isTrait;
	protected String name;
	protected Import im;
	protected SymbolTable table;
	
	public Symbol(){
		this("", false, false, false, false);
	}
	
	public Symbol(String name){
		this(name, false, false, false, false);
	}
	
	public Symbol(String name, boolean isFinal){
		this(name, isFinal, false, false, false);
	}
	
	public Symbol(String name, boolean isFinal, boolean isShared) {
		this(name, isFinal, isShared, false, false);
	}
	
	public Symbol(String name, boolean isFinal, boolean isShared, boolean isClosure) {
		this(name, isFinal, isShared, isClosure, false);
	}
	
	public Symbol(String name, boolean isFinal, boolean isShared, boolean isClosure, boolean isTrait){
		this.name = name;
		this.isFinal = isFinal;
		this.isShared = isShared;
		this.isClosure = isClosure;
		this.isTrait = isTrait;
	}

	public boolean isShared(){
		return isShared;
	}

	public void setShared(boolean isShared){
		this.isShared = isShared;
	}

	public boolean isFinal(){
		return isFinal;
	}

	public void setFinal(boolean isFinal){
		this.isFinal = isFinal;
	}
	
	public boolean isClosure() {
		return isClosure;
	}
	
	public void setClosure(boolean isClosure) {
		this.isClosure = isClosure;
	}

	public boolean isTrait() {
		return isTrait;
	}
	
	public void setTrait(boolean isTrait) {
		this.isTrait = isTrait;
	}
	
	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public SymbolTable getTable() {
		return table;
	}

	public SymbolTable.Scope getDeclaringScope(){
		return table.getScope();
	}

	public void setTable(SymbolTable table) {
		this.table = table;
	}

	public void setImport(Import im){
		this.im = im;
	}

	public Import getImport(){
		return im;
	}

	public boolean isImported(){
		return im != null;
	}

	@Override
	public boolean equals(Object other){
		if(other instanceof Symbol){
			Symbol otherSymbol = (Symbol) other;
			return otherSymbol.getName().equals(name);
		}
		return false;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		if(isShared){
			builder.append("shared ");
		}
		
		if(isFinal){
			builder.append("final ");
		}

		builder.append(name);

		builder.append(" scope: ");
		builder.append(getDeclaringScope());
		
		return builder.toString();
	}
	
}

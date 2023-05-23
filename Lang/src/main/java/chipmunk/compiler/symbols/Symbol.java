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

		public Import clone(){
			return new Import(module, aliased);
		}

	}
	
	protected boolean isShared;
	protected boolean isFinal;
	protected boolean isUpvalue;
	protected boolean isTrait;
	protected String name;
	protected Import im;
	protected SymbolType type;
	protected SymbolTable table;
	protected Symbol upvalueRef;
	
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
	
	public Symbol(String name, boolean isFinal, boolean isShared, boolean isUpvalue) {
		this(name, isFinal, isShared, isUpvalue, false);
	}
	
	public Symbol(String name, boolean isFinal, boolean isShared, boolean isUpvalue, boolean isTrait){
		this.name = name;
		this.isFinal = isFinal;
		this.isShared = isShared;
		this.isUpvalue = isUpvalue;
		this.isTrait = isTrait;
		type = SymbolType.VAR;
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
	
	public boolean isUpvalue() {
		return isUpvalue;
	}
	
	public void markAsUpvalue() {
		isUpvalue = true;
	}

	public void setUpvalueRef(Symbol ref){
		upvalueRef = ref;
	}

	public Symbol getUpvalueRef(){
		return upvalueRef;
	}

	public boolean isUpvalueRef(){
		return upvalueRef != null;
	}

	public Symbol makeUpvalueRef(){
		var ref = clone();
		ref.isUpvalue = false;
		ref.setUpvalueRef(this);
		return ref;
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

	public SymbolType getType(){
		return type;
	}

	public void setType(SymbolType type){
		this.type = type;
	}

	public Symbol clone(){
		Symbol clone = new Symbol(name, isFinal, isShared, isUpvalue, isTrait);
		clone.setTable(table);
		clone.setType(type);
		clone.setUpvalueRef(upvalueRef);
		if(isImported()){
			clone.setImport(im.clone());
		}
		return clone;
	}

	@Override
	public boolean equals(Object other){
		if(other instanceof Symbol otherSymbol){
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

		if(table != null){
			builder.append(" scope: ");
			builder.append(getDeclaringScope());
		}

		if(isUpvalue()){
			builder.append(" upvalue ");
		}

		if(isUpvalueRef()){
			builder.append(" upvalueRef: ");
			builder.append(upvalueRef.getName());
		}

		builder.append(" type: ");
		builder.append(type);

		if(isImported()){
			builder.append(" imported from ");
			builder.append(getImport().getModule());
			if(getImport().isAliased()){
				builder.append(" with alias ");
				builder.append(getImport().getAliasedSymbol());
			}
		}
		
		return builder.toString();
	}
	
}

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

package chipmunk.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class ImportNode extends AstNode {

	protected String module;
	protected boolean importAll;
	protected List<String> symbols;
	protected List<String> aliases;
	
	public ImportNode(){
		super();
		importAll = false;
		symbols = new ArrayList<>();
		aliases = new ArrayList<>();
	}
	
	public String getModule(){
		return module;
	}
	
	public void setModule(String modName){
		module = modName;
	}
	
	public void addSymbol(String symbol){
		symbols.add(symbol);
	}
	
	public void addAlias(String alias){
		aliases.add(alias);
	}
	
	public void removeSymbol(String symbol){
		symbols.remove(symbol);
	}
	
	public void removeAlias(String alias){
		aliases.remove(alias);
	}
	
	public boolean hasSymbol(String symbol){
		return symbols.contains(symbol);
	}
	
	public List<String> getSymbols(){
		return symbols;
	}
	
	public List<String> getAliases(){
		return aliases;
	}
	
	public boolean hasAliases(){
		return aliases.size() != 0;
	}
	
	public void setImportAll(boolean importAll){
		this.importAll = importAll;
	}
	
	public boolean isImportAll(){
		return importAll;
	}
	
	@Override
	public String getDebugName(){
		StringBuilder builder = new StringBuilder();
		builder.append("import ");
		
		int symbol = 0;
		while(symbol < symbols.size()){
			builder.append(symbols.get(symbol));
			
			if(symbol < symbols.size() - 1){
				builder.append(',');
			}else{
				builder.append(' ');
			}
			symbol++;
		}
		
		if(aliases.size() > 0){
			builder.append("as ");
		}
		
		int alias = 0;
		while(alias < aliases.size()){
			builder.append(aliases.get(alias));
			
			if(alias < aliases.size() - 1){
				builder.append(',');
			}else{
				builder.append(' ');
			}
			alias++;
		}
		
		if(symbols.size() > 0){
			builder.append("from ");
		}
		builder.append(module);

		return builder.toString();
	}
}

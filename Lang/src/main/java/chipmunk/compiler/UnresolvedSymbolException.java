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

public class UnresolvedSymbolException extends RuntimeException {
	
	private static final long serialVersionUID = -3751232792577027254L;

	protected final String moduleName;
	protected final String symbolName;
	
	public UnresolvedSymbolException(String moduleName, String symbolName){
		super(String.format("%s is not defined or imported in module %s", symbolName, moduleName));
		this.moduleName = moduleName;
		this.symbolName = symbolName;
	}

	public String getModuleName(){
		return moduleName;
	}

	public String getSymbolName(){
		return symbolName;
	}
}

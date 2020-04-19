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

package chipmunk.modules.lang;


public abstract class CType extends CObject {

	protected String name;
	protected CModule module;
	
	public CType(){
		super();
		name = "";
		type = this;
	}
	
	public CType(String typeName){
		name = typeName;
		type = this;
	}
	
	public void setName(String typeName){
		name = typeName;
	}
	
	public String getName(){
		return name;
	}
	
	public CModule getModule(){
		return module;
	}
	
	public abstract CObject instance();
	
	public void setModule(CModule typeModule){
		module = typeModule;
		module.getNamespace().set(name, this);
	}
	
}

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

package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;

public class CString {
	
	private String value;
	
	public CString(){
		value = "";
	}
	
	public CString(String value){
		this.value = value;
	}
	
	public String stringValue(){
		return value;
	}

	public CInteger hashCode(ChipmunkVM vm){
		vm.traceInteger();
		return new CInteger(hashCode());
	}

	public CString plus(ChipmunkVM vm, Object other){
		String newValue = value + other != null ? other.toString() : "null";
		vm.traceString(newValue);
		return new CString(newValue);
	}

	public CBoolean equals(ChipmunkVM vm, Object other){
		vm.traceBoolean();
		return new CBoolean(equals(other));
	}
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object other){
		if(other != null && other instanceof CString){
			if(value.equals(((CString) other).value)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		return value;
	}

}

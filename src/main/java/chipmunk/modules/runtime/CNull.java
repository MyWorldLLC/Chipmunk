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
import chipmunk.RuntimeObject;

public class CNull implements RuntimeObject {
	
	private static CNull instance;
	
	private CNull() {}
	
	public static CNull instance() {
		if(instance == null) {
			instance = new CNull();
		}
		return instance;
	}

	public CBoolean truth(ChipmunkVM context){
		context.traceMem(1);
		return new CBoolean(false);
	}
	
	public String toString(){
		return "CNull";
	}

	public CBoolean equals(ChipmunkVM vm, Object other){
		vm.traceBoolean();
		return new CBoolean(equals(other));
	}
	
	public boolean equals(Object other){
		if(other instanceof CNull){
			return true;
		}
		return false;
	}
}

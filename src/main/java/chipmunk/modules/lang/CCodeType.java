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

import chipmunk.ChipmunkVM;

public class CCodeType extends CType {

	public CCodeType(){
		super("Code");
	}
	@Override
	public CObject instance() {
		return new CCode();
	}
	
	@Override
	public CObject __call__(ChipmunkVM context, int params, boolean resuming){
		if(params == 0){
			return new CCode();
		}else{
			throw new UnimplementedOperationChipmunk("CCodeType.__call__() is not defined for parameter count: " + params);
		}
	}

}

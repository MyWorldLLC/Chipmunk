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

public class CFloatType extends CType {

	protected CFloat tempValue;
	
	public CFloatType(){
		super("Float");
		tempValue = new CFloat();
		tempValue.type = this;
		tempValue.namespace.set("type", this);
	}
	
	public CFloat getTemp(){
		return tempValue;
	}
	
	public CObject instance(){
		return new CFloat();
	}
	
	/*@Override
	public CObject __call__(ChipmunkContext context, int params, boolean resuming){
		if(params == 0){
			return new CFloat();
		}else if(params == 1){
			return new CFloat(((CFloat)(context.pop().__as__(this))).getValue());
		}else{
			throw new UnimplementedOperationChipmunk("CFloatType.__call__() is not defined for parameter count: " + params);
		}
	}*/
}

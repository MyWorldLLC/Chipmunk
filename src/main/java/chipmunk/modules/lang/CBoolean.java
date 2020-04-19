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

import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;

public class CBoolean extends CObject {

	protected boolean boolValue;
	
	public CBoolean(){
		super();
		boolValue = false;
	}
	
	public CBoolean(boolean value){
		super();
		boolValue = value;
	}
	
	public boolean getValue(){
		return boolValue;
	}
	
	public void setValue(boolean value){
		boolValue = value;
	}
	
	public boolean __truth__(){
		return boolValue;
	}
	
	public CObject __as__(CObject convertType){
		if(convertType instanceof CIntType){
			return new CInt(boolValue ? 1 : 0);
		}else if(convertType instanceof CFloatType){
			return new CFloat(boolValue ? 1 : 0);
		}else if(convertType instanceof CBoolean){
			return new CBoolean(this.__truth__());
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform bool as %s", convertType.getClass().getSimpleName()));
		}
		
	}
	
	public int __compare__(CObject other){
		if(other instanceof CBoolean){
			return Boolean.compare(boolValue, ((CBoolean) other).boolValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot compare bool to %s", other.getClass().getSimpleName()));
		}
	}
	
	public void __pack__(NutPacker packer, OutputCapsule out){
		out.write(boolValue);
	}
	
	public void __unpack__(NutCracker cracker, InputCapsule in){
		boolValue = in.readBoolean();
	}
	
	public int __hash__(){
		return Boolean.hashCode(boolValue);
	}
	
	public CString __string__(){
		return new CString(Boolean.toString(boolValue));
	}
	
}

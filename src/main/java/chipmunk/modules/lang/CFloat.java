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

import chipmunk.modules.lang.CObject;
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;

public class CFloat extends CObject {
	
	protected float floatValue;
	
	public CFloat(){
		super();
		floatValue = 0.0f;
	}
	
	public CFloat(float value){
		super();
		floatValue = value;
	}
	
	public float getValue(){
		return floatValue;
	}
	
	@Override
	public CObject __plus__(CObject other){
		
		if(other instanceof CFloat){
			return new CFloat(floatValue + ((CFloat) other).floatValue);
		}else if(other instanceof CInt){
			return new CFloat(floatValue + (float)((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform float + %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __minus__(CObject other){
		if(other instanceof CFloat){
			return new CFloat(floatValue - ((CFloat) other).floatValue);
		}else if(other instanceof CInt){
			return new CFloat(floatValue - (float)((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform float - %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __mul__(CObject other){
		if(other instanceof CFloat){
			return new CFloat(floatValue * ((CFloat) other).floatValue);
		}else if(other instanceof CInt){
			return new CFloat(floatValue * (float)((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform float * %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __div__(CObject other){
		if(other instanceof CFloat){
			return new CFloat((float)floatValue / ((CFloat) other).floatValue);
		}else if(other instanceof CInt){
			return new CFloat(floatValue / (float)((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform float / %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __fdiv__(CObject other){
		if(other instanceof CFloat){
			return new CInt((int)(floatValue / ((CFloat) other).floatValue));
		}else if(other instanceof CInt){
			return new CInt((int)(floatValue / ((CInt) other).intValue));
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform float // %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __rem__(CObject other){
		if(other instanceof CFloat){
			return new CFloat(floatValue % ((CFloat) other).floatValue);
		}else if(other instanceof CInt){
			return new CFloat(floatValue % ((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform float % %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __pow__(CObject other){
		if(other instanceof CFloat){
			return new CFloat((float)Math.pow(floatValue, ((CFloat) other).floatValue));
		}else if(other instanceof CInt){
			return new CFloat((float)Math.pow(floatValue, ((CInt) other).intValue));
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform float ** %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __inc__(){
		return new CFloat(floatValue + 1);
	}
	
	public CObject __dec__(){
		return new CFloat(floatValue - 1);
	}
	
	public CObject __pos__(){
		return new CFloat(Math.abs(floatValue));
	}
	
	public CObject __neg__(){
		return new CFloat(-floatValue);
	}
	
	public boolean __truth__(){
		return floatValue != 0 ? true : false;
	}
	
	public CObject __as__(CObject convertType){
		if(convertType instanceof CFloatType){
			return new CFloat(floatValue);
		}else if(convertType instanceof CFloatType){
			return new CFloat(floatValue);
		}else if(convertType instanceof CBoolean){
			return new CBoolean(this.__truth__());
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform float as %s", convertType.getClass().getSimpleName()));
		}
		
	}
	
	public int __compare__(CObject other){
		if(other instanceof CFloat){
			return Float.compare(floatValue, ((CFloat) other).floatValue);
		}else if(other instanceof CInt){
			return Float.compare(floatValue, ((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot compare float to %s", other.getClass().getSimpleName()));
		}
	}
	
	public void __pack__(NutPacker packer, OutputCapsule out){
		out.write(floatValue);
	}
	
	public void __unpack__(NutCracker cracker, InputCapsule in){
		floatValue = in.readFloat();
	}
	
	public int __hash__(){
		return Float.hashCode(floatValue);
	}
	
	public CString __string__(){
		return new CString(Float.toString(floatValue));
	}
}

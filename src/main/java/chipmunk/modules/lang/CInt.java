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

public class CInt extends CObject {

	protected int intValue;
	
	public CInt(){
		super();
		intValue = 0;
	}
	
	public CInt(int value){
		super();
		intValue = value;
	}
	
	public int getValue(){
		return intValue;
	}
	
	public void setValue(int value){
		intValue = value;
	}
	
	@Override
	public CObject __plus__(CObject other){
		
		if(other instanceof CInt){
			return new CInt(intValue + ((CInt) other).intValue);
		}else if(other instanceof CFloat){
			return new CFloat((float)intValue + ((CFloat) other).floatValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int + %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __minus__(CObject other){
		if(other instanceof CInt){
			return new CInt(intValue - ((CInt) other).intValue);
		}else if(other instanceof CFloat){
			return new CFloat((float)intValue - ((CFloat) other).floatValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int - %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __mul__(CObject other){
		if(other instanceof CInt){
			return new CInt(intValue * ((CInt) other).intValue);
		}else if(other instanceof CFloat){
			return new CFloat((float)intValue * ((CFloat) other).floatValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int * %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __div__(CObject other){
		if(other instanceof CInt){
			return new CFloat((float)intValue / ((CInt) other).intValue);
		}else if(other instanceof CFloat){
			return new CFloat((float)intValue / ((CFloat) other).floatValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int / %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __fdiv__(CObject other){
		if(other instanceof CInt){
			return new CInt(intValue / ((CInt) other).intValue);
		}else if(other instanceof CFloat){
			return new CInt((int)(intValue / ((CFloat) other).floatValue));
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int // %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __rem__(CObject other){
		if(other instanceof CInt){
			return new CInt(intValue % ((CInt) other).intValue);
		}else if(other instanceof CFloat){
			return new CFloat((float)intValue % ((CFloat) other).floatValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int % %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __pow__(CObject other){
		if(other instanceof CInt){
			return new CInt((int)Math.pow(intValue, ((CInt) other).intValue));
		}else if(other instanceof CFloat){
			return new CFloat((float)Math.pow(intValue, ((CFloat) other).floatValue));
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int ** %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __inc__(){
		return new CInt(intValue + 1);
	}
	
	public CObject __dec__(){
		return new CInt(intValue - 1);
	}
	
	public CObject __pos__(){
		return new CInt(Math.abs(intValue));
	}
	
	public CObject __neg__(){
		return new CInt(-intValue);
	}
	
	public CObject __bxor__(CObject other){
		if(other instanceof CInt){
			return new CInt(intValue ^ ((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int ^ %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __band__(CObject other){
		if(other instanceof CInt){
			return new CInt(intValue & ((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int & %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __bor__(CObject other){
		if(other instanceof CInt){
			return new CInt(intValue | ((CInt) other).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int | %s", other.getClass().getSimpleName()));
		}
	}
	
	public CObject __bneg__(){
		return new CInt(~intValue);
	}
	
	public CObject __lshift__(CObject places){
		if(places instanceof CInt){
			return new CInt(intValue << ((CInt) places).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int << %s", places.getClass().getSimpleName()));
		}
	}
	
	public CObject __rshift__(CObject places){
		if(places instanceof CInt){
			return new CInt(intValue >> ((CInt) places).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int >> %s", places.getClass().getSimpleName()));
		}
	}
	
	public CObject __urshift__(CObject places){
		if(places instanceof CInt){
			return new CInt(intValue >>> ((CInt) places).intValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int >>> %s", places.getClass().getSimpleName()));
		}
	}
	
	public boolean __truth__(){
		return intValue != 0 ? true : false;
	}
	
	public CObject __as__(CObject convertType){
		if(convertType instanceof CIntType){
			return new CInt(intValue);
		}else if(convertType instanceof CFloatType){
			return new CFloat(intValue);
		}else if(convertType instanceof CBoolean){
			return new CBoolean(this.__truth__());
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int as %s", convertType.getClass().getSimpleName()));
		}
		
	}
	
	public int __compare__(CObject other){
		if(other instanceof CInt){
			return Integer.compare(intValue, ((CInt) other).intValue);
		}else if(other instanceof CFloat){
			return Float.compare(intValue, ((CFloat) other).floatValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot compare int to %s", other.getClass().getSimpleName()));
		}
	}
	
	public void __pack__(NutPacker packer, OutputCapsule out){
		out.write(intValue);
	}
	
	public void __unpack__(NutCracker cracker, InputCapsule in){
		intValue = in.readInt();
	}
	
	public int __hash__(){
		return Integer.hashCode(intValue);
	}
	
	public CString __string__(){
		return new CString(Integer.toString(intValue));
	}
	
}

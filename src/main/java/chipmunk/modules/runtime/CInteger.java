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
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;

public class CInteger implements RuntimeObject {

	private int value;
	
	public CInteger(int value){
		this.value = value;
	}
	
	public CInteger(){
		value = 0;
	}
	
	public int getValue(){
		return value;
	}

	//public boolean booleanValue() {
	//	return value != 0;
	//}
	
	public int intValue(){
		return value;
	}
	
	public float floatValue(){
		return value;
	}
	
	public CInteger plus(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value + other.value);
	}
	
	public CFloat plus(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value + other.floatValue());
	}
	
	public CInteger minus(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value - other.value);
	}
	
	public CFloat minus(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value - other.floatValue());
	}
	
	public CInteger mul(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value * other.value);
	}
	
	public CFloat mul(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value * other.floatValue());
	}
	
	public CFloat div(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CFloat(((float)value) / other.value);
	}
	
	public CFloat div(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value / other.floatValue());
	}
	
	public CInteger fdiv(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value / other.value);
	}
	
	public CInteger fdiv(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CInteger((int) (value / other.floatValue()));
	}
	
	public CInteger mod(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value % other.value);
	}
	
	public CFloat mod(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value % other.floatValue());
	}
	
	public CInteger pow(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger((int) Math.pow(value, other.value));
	}
	
	public CFloat pow(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat((float) Math.pow(value, other.floatValue()));
	}
	
	public CInteger inc(ChipmunkVM context){
		context.traceMem(4);
		return new CInteger(value + 1);
	}
	
	public CInteger dec(ChipmunkVM context){
		context.traceMem(4);
		return new CInteger(value - 1);
	}
	
	public CInteger pos(ChipmunkVM context){
		context.traceMem(4);
		return new CInteger(Math.abs(value));
	}
	
	public CInteger neg(ChipmunkVM context){
		context.traceMem(4);
		return new CInteger(-value);
	}
	
	public CInteger bxor(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value ^ other.value);
	}
	
	public CInteger band(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value & other.value);
	}
	
	public CInteger bor(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value | other.value);
	}
	
	public CInteger bneg(ChipmunkVM context){
		context.traceMem(4);
		return new CInteger(~value);
	}
	
	public CInteger lshift(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value << other.value);
	}
	
	public CInteger rshift(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value >> other.value);
	}
	
	public CInteger urshift(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger(value >>> other.value);
	}
	
	public CBoolean truth(ChipmunkVM context){
		context.traceMem(4);
		return value != 0 ? new CBoolean(true) : new CBoolean(false);
	}
	
	public Object as(ChipmunkVM context, Class<?> otherType){
		if(otherType == CInteger.class){
			context.traceMem(4);
			return new CInteger(value);
		}else if(otherType == CFloat.class){
			context.traceMem(4);
			return new CFloat(value);
		}else if(otherType == CBoolean.class){
			return truth(context);
		}else{
			throw new BadConversionChipmunk(String.format("Cannot convert int to %s", otherType.getClass().getSimpleName()), this, otherType);
		}
	}
	
	public CInteger compare(ChipmunkVM context, CInteger other){
		return new CInteger(Integer.compare(value, other.value));
	}
	
	public CInteger compare(ChipmunkVM context, CFloat other){
		return new CInteger(Float.compare(value, other.floatValue()));
	}
	
	public CIntegerRange range(ChipmunkVM vm, CInteger other, Boolean inclusive){
		if(value <= other.value){
			return new CIntegerRange(value, other.value, 1, inclusive.booleanValue());
		}else{
			return new CIntegerRange(value, other.value, -1, inclusive.booleanValue());
		}
	}
	
	public CFloatRange range(ChipmunkVM vm, CFloat other, Boolean inclusive){
		if(value <= other.floatValue()){
			return new CFloatRange(value, other.getValue(), 1, inclusive.booleanValue());
		}else{
			return new CFloatRange(value, other.getValue(), -1, inclusive.booleanValue());
		}
	}
	
	public void pack(ChipmunkVM context, NutPacker packer, OutputCapsule out){
		out.write(value);
	}
	
	public void unpack(ChipmunkVM context, NutCracker cracker, InputCapsule in){
		context.traceMem(4);
		value = in.readInt();
	}
	
	public CInteger hash(ChipmunkVM context){
		context.traceMem(4);
		return new CInteger(hashCode());
	}
	
	public int hashCode(){
		return Integer.hashCode(value);
	}
	
	public CString string(ChipmunkVM context){
		String stringValue = toString();
		context.traceMem(stringValue.length() * 2);
		return new CString(stringValue);
	}
	
	public String toString(){
		return Integer.toString(value);
	}
	
	public CBoolean equals(ChipmunkVM context, CInteger other){
		context.traceMem(1);
		return new CBoolean(value == other.value);
	}
	
	public CBoolean equals(ChipmunkVM context, CFloat other){
		context.traceMem(1);
		return new CBoolean(((float)value) == other.floatValue());
	}
	
	public boolean equals(Object other){
		if(other instanceof CInteger){
			if(((CInteger) other).value == value){
				return true;
			}
		}
		return false;
	}
}

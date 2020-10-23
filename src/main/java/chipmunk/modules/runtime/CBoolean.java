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
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;

public class CBoolean {
	
	private boolean value;
	
	public CBoolean(boolean value){
		this.value = value;
	}
	
	public boolean getValue(){
		return value;
	}
	
	public boolean booleanValue(){
		return value;
	}
	
	public CBoolean truth(ChipmunkVM context){
		return this;
	}
	
	public Object as(ChipmunkVM context, Class<?> otherType){
		if(otherType == CInteger.class){
			//context.traceMem(4);
			return new CInteger(value ? 1 : 0);
		}else if(otherType == CFloat.class){
			//context.traceMem(4);
			return new CFloat(value ? 1.0f : 0.0f);
		}else if(otherType == CBoolean.class){
			return this;
		}else{
			throw new TypeConversionException(String.format("Cannot convert boolean to %s", otherType.getClass().getSimpleName()), this, otherType);
		}
	}
	
	public CInteger compare(ChipmunkVM context, CBoolean other){
		return new CInteger(Boolean.compare(value, other.value));
	}
	
	public void pack(ChipmunkVM context, NutPacker packer, OutputCapsule out){
		out.write(value);
	}
	
	public void unpack(ChipmunkVM context, NutCracker cracker, InputCapsule in){
		//context.traceMem(4);
		value = in.readBoolean();
	}
	
	public CInteger hash(ChipmunkVM context){
		//context.traceMem(4);
		return new CInteger(hashCode());
	}
	
	public int hashCode(){
		return Boolean.hashCode(value);
	}
	
	public String string(ChipmunkVM context){
		String stringValue = toString();
		//context.traceMem(stringValue.length() * 2);
		return stringValue;
	}

	public CString toString(ChipmunkVM vm){
		String str = Boolean.toString(value);
		//vm.traceString(str);
		return new CString(str);
	}
	public String toString(){
		return Boolean.toString(value);
	}
	
	public CBoolean equals(ChipmunkVM context, CBoolean other){
		return new CBoolean (value == other.value);
	}
	
	public boolean equals(Object other){
		if(other instanceof CBoolean){
			if(((CBoolean) other).value == value){
				return true;
			}
		}
		return false;
	}

}

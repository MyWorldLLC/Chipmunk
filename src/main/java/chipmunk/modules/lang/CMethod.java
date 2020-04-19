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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMethod extends CObject {
	
	protected CMethod outer;
	protected int argCount;
	protected int localCount;
	protected Map<String, Integer> localNames;
	
	protected byte[] instructions;
	protected CCode code;
	protected List<CObject> constantPool;
	
	public CMethod(){
		super();
		localNames = new HashMap<String, Integer>();
	}

	public CMethod getOuter(){
		return outer;
	}
	
	public void setOuter(CMethod outer){
		this.outer = outer;
	}
	
	public int getArgCount(){
		return argCount;
	}
	
	public void setArgCount(int count){
		argCount = count;
	}
	
	public int getLocalCount(){
		return localCount;
	}
	
	public void setLocalCount(int count){
		localCount = count;
	}
	
	public CCode getCode(){
		return code;
	}
	
	public void setCode(CCode code){
		this.code = code;
		instructions = code.getCode();
	}
	
	public void setConstantPool(List<CObject> constantPool){
		this.constantPool = constantPool;
	}
	
	public List<CObject> getConstantPool(){
		return constantPool;
	}

	public int getLocalIndex(String name){
		
		Integer index = localNames.get(name);
		
		if(index != null){
			return index;
		}else{
			return -1;
		}
		
	}
	
	public void setLocalIndex(String name, int index){
		localNames.put(name, index);
	}
	
}
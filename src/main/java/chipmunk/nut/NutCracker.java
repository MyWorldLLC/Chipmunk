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

package chipmunk.nut;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import chipmunk.ChipmunkVM;
import chipmunk.modules.lang.CObject;
import chipmunk.modules.lang.CType;
import chipmunk.modules.runtime.CModule;

public class NutCracker {

	public static final byte NUT_VERSION = (byte) 0x10;
	
	protected List<CObject> instances;
	protected Deque<Integer> primaries;
	
	public NutCracker(){
		instances = new ArrayList<CObject>();
		primaries = new ArrayDeque<Integer>();
	}
	
	public void reset(){
		instances.clear();
		primaries.clear();
	}
	
	public CObject getInstance(int index){
		return instances.get(index);
	}
	
	public Nut unpack(ChipmunkVM context, InputStream in){
		
		InputCapsule capsule = new InputCapsule(in);
		
		// check that this is really nut data
		for(int i = 0; i < NutFormat.MAGIC_NUMBER.length; i++){
			byte next = capsule.readByte();
			if(NutFormat.MAGIC_NUMBER[i] != next){
				throw new NutFormatChipmunk("Invalid nut, magic number does not match");
			}
		}
		
		// check that the file version matches the cracker version
		byte version = capsule.readByte();
		if(version != NUT_VERSION){
			throw new NutFormatChipmunk("Bad nut version. Expected " + NUT_VERSION + ", got " + version);
		}
		
		Nut nut = new Nut();
		
		// read metadata table
		byte marker = capsule.readByte();
		if(marker != NutFormat.TABLE_MARKER){
			throw new NutFormatChipmunk("Invalid nut data while attempting to read metadata table. Expected opening TABLE_MARKER, encountered " + marker);
		}
		
		int metadataSize = capsule.readInt();
		for(int i = 0; i < metadataSize; i++){
			
			String name = capsule.readString();
			String value = capsule.readString();
			nut.setMetaData(name, value);
			
		}
		
		marker = capsule.readByte();
		if(marker != NutFormat.TABLE_MARKER){
			throw new NutFormatChipmunk("Invalid nut data while attempting to read metadata table. Expected ending TABLE_MARKER, encountered " + marker);
		}
		
		// read type table, resolve types
		marker = capsule.readByte();
		if(marker != NutFormat.TABLE_MARKER){
			throw new NutFormatChipmunk("Invalid nut data while attempting to read type table. Expected opening TABLE_MARKER, encountered " + marker);
		}
		
		int typeCount = capsule.readInt();
		List<CType> types = new ArrayList<CType>(typeCount);
		
		for(int i = 0; i < typeCount; i++){
			
			String typeName = capsule.readString();
			String moduleName = capsule.readString();
			
			CModule module = context.resolveModule(moduleName);
			Object symbolAttr = module.getNamespace().get(typeName);
			
			if(symbolAttr == null){
				throw new MissingTypeChipmunk("Could not load type " + typeName + " from module " + moduleName + ": module does not contain symbol " + typeName);
			}
			
			if(symbolAttr instanceof CType){
				types.add((CType) symbolAttr);
			}else{
				throw new MissingTypeChipmunk("Could not load type " + typeName + " from module " + moduleName + ": symbol " + typeName + "does not resolve to a type.");
			}
		}
		
		marker = capsule.readByte();
		if(marker != NutFormat.TABLE_MARKER){
			throw new NutFormatChipmunk("Invalid nut data while attempting to read type table. Expected ending TABLE_MARKER, encountered " + marker);
		}
		
		// read instance table, create instances
		marker = capsule.readByte();
		if(marker != NutFormat.TABLE_MARKER){
			throw new NutFormatChipmunk("Invalid nut data while attempting to read instance table. Expected beginning TABLE_MARKER, encountered " + marker);
		}
		
		int instanceCount = capsule.readInt();
		for(int i = 0; i < instanceCount; i++){
			
			marker = capsule.readByte();
			if(marker == NutFormat.PRIMARY_INSTANCE){
				primaries.add(i);
			}else if(marker != NutFormat.SECONDARY_INSTANCE){
				throw new NutFormatChipmunk("Invalid nut data while attempting to read instance table. Expected PRIMARY_INSTANCE or SECONDARY_INSTANCE, encountered " + marker);
			}
			
			int typeIndex = capsule.readInt();
			CType type = types.get(typeIndex);
			CObject instance = null;//type.instance();
			instances.add(instance);
		}
		
		marker = capsule.readByte();
		if(marker != NutFormat.TABLE_MARKER){
			throw new NutFormatChipmunk("Invalid nut data while attempting to read instance table. Expected ending TABLE_MARKER, encountered " + marker);
		}
		
		// unpack instances
		marker = capsule.readByte();
		if(marker != NutFormat.TABLE_MARKER){
			throw new NutFormatChipmunk("Invalid nut data while attempting to read data table. Expected beginning TABLE_MARKER, encountered " + marker);
		}
		
		for(int i = 0; i < instances.size(); i++){
			CObject instance = instances.get(i);
			instance.__unpack__(this, capsule);
		}
		
		marker = capsule.readByte();
		if(marker != NutFormat.TABLE_MARKER){
			throw new NutFormatChipmunk("Invalid nut data while attempting to read data table. Expected ending TABLE_MARKER, encountered " + marker);
		}
		
		// copy primary references to nut
		for(int i = 0; i < instances.size(); i++){
			Integer nextPrimary = primaries.peek();
			if(nextPrimary != null && i == nextPrimary.intValue()){
				nut.addInstance(instances.get(i));
				primaries.pop();
			}
		}
		
		return nut;
	}
}

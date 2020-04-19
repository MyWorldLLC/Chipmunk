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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.modules.lang.CObject;

public class Nut {
	
	protected Map<String, String> metadata;
	protected List<CObject> instances;
	
	public Nut(){
		
		metadata = new HashMap<String, String>();
		instances = new ArrayList<CObject>();
		
	}

	public String getMetaData(String name){
		return metadata.get(name);
	}
	
	public Map<String, String> getMetaData(){
		return metadata;
	}
	
	public void setMetaData(String name, String value){
		metadata.put(name, value);
	}
	
	public void addInstance(CObject obj){
		instances.add(obj);
	}
	
	public List<CObject> getInstances(){
		return instances;
	}
	
}
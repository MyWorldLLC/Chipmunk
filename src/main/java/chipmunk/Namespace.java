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

package chipmunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chipmunk.modules.runtime.*;

public class Namespace {

	private final Map<String, Object> attributes;
	private List<Namespace> traitSpaces;
	private Set<String> closures;
	private List<String> traits;
	private Set<String> finalAttributes;
	
	public Namespace(){
		attributes = new HashMap<>();
	}
	
	public boolean has(String name){
		return attributes.containsKey(name);
	}
	
	public Object get(String name){
		if(closures != null && closures.contains(name)) {
			return ((CClosure) attributes.get(name)).get();
		}
		
		Object result = attributes.get(name);
		// we don't have this ourselves - search our traits to see if one of them
		// has the value
		if(traits != null && result == null) {
			for(int i = 0; i < traits.size(); i++) {

				Object trait = attributes.get(traits.get(i));

				if(trait instanceof CObject){
					Object value = ((CObject) trait).getAttributes().get(name);
					if(value != null) {
						return value;
					}
				}
			}
		}
		return result;
	}
	
	public void set(String name, Object value) throws IllegalArgumentException {
		
		if(value == null){
			value = CNull.instance();
		}
		
		if(finalAttributes != null && finalAttributes.contains(name)){
			throw new IllegalArgumentException("Cannot set final attribute: " + name);
		}
		
		if(closures != null && closures.contains(name)) {
			CClosure closure = (CClosure) attributes.get(name);
			closure.set(value);
			return;
		}
		
		attributes.put(name, value);
	}
	
	public void setFinal(String name, Object value){
		
		if(value == null){
			value = CNull.instance();
		}

		markFinal(name);
		attributes.put(name, value);
	}

	public void markFinal(String name){
		if(finalAttributes == null){
			finalAttributes = new HashSet<>();
		}

		finalAttributes.add(name);
	}
	
	public void setTrait(String name, Object value) {
		markTrait(name);
		attributes.put(name, value);
	}

	public void markTrait(String name){
		if(traits == null) {
			traits = new ArrayList<>(1);
		}

		if(!traits.contains(name)){
			traits.add(name);
		}
	}
	
	public Set<String> names(){
		return attributes.keySet();
	}
	
	public Set<String> finalNames(){
		if(finalAttributes == null){
			return null;
		}
		return Collections.unmodifiableSet(finalAttributes);
	}

	public List<String> traitNames(){
		if(traits == null){
			return null;
		}

		return Collections.unmodifiableList(traits);
	}
	
	public List<Object> traitAttributes(){
		List<Object> traitAttributes = new ArrayList<>(traits.size());
		for(String trait : traits) {
			traitAttributes.add(get(trait));
		}
		return traitAttributes;
	}
	
	public Namespace duplicate(){
		Namespace dup = new Namespace();

		for(Map.Entry<String, Object> entry : attributes.entrySet()){
			Object value = entry.getValue();

			if(value instanceof CMethod){
				value = ((CMethod) value).duplicate();
			}

			dup.attributes.put(entry.getKey(), value);
		}

		if(finalAttributes != null) {
			dup.finalAttributes = new HashSet<String>();
			dup.finalAttributes.addAll(finalAttributes);
		}
		
		if(traits != null) {
			dup.traits = new ArrayList<String>(traits.size());
			dup.traits.addAll(traits);
		}
		
		if(traitSpaces != null) {
			dup.traitSpaces = new ArrayList<Namespace>(traitSpaces.size());
			dup.traitSpaces.addAll(traitSpaces);
		}
		
		return dup;
	}
}

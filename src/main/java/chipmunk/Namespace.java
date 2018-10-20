package chipmunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CClosure;
import chipmunk.modules.runtime.CNull;
import chipmunk.modules.runtime.CObject;

public class Namespace {

	private final Map<String, Object> attributes;
	private List<Namespace> traitSpaces;
	private Set<String> closures;
	private Set<String> traits;
	private Set<String> finalAttributes;
	
	public Namespace(){
		attributes = new HashMap<String, Object>();
	}
	
	public boolean has(String name){
		return attributes.containsKey(name);
	}
	
	public Object get(String name){
		if(closures != null && closures.contains(name)) {
			return ((CClosure) attributes.get(name)).get();
		}
		// we don't have this ourselves - search our traits to see if one of them
		// has the value
		if(traits != null && !attributes.containsKey(name)) {
			for(int i = 0; i < traitSpaces.size(); i++) {
				Object value = traitSpaces.get(i);
				if(value != null) {
					return value;
				}
			}
		}
		return attributes.get(name);
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
		
		if(traits != null && traits.contains(name)) {
			Object oldTrait = attributes.get(name);
			if(oldTrait instanceof CObject) {
				unlink(((CObject) oldTrait).getAttributes());
			}else {
				unlink(((CClass) oldTrait).getAttributes());
			}
			
			if(value instanceof CObject) {
				link(((CObject) value).getAttributes());
			}else {
				link(((CClass) value).getAttributes());
			}
		}
		
		attributes.put(name, value);
	}
	
	private void link(Namespace space) {
		if(traitSpaces == null) {
			traitSpaces = new ArrayList<Namespace>(1);
		}
		traitSpaces.add(space);
	}
	
	private void unlink(Namespace space) {
		for(int i = 0; i < traitSpaces.size(); i++) {
			if(traitSpaces.get(i) == space) {
				traitSpaces.remove(i);
			}
		}
	}
	
	public void setFinal(String name, Object value){
		
		if(value == null){
			value = CNull.instance();
		}
		
		if(finalAttributes == null){
			finalAttributes = new HashSet<String>();
		}
		
		finalAttributes.add(name);
		attributes.put(name, value);
	}
	
	public void setClosure(String name, CClosure closure) {
		if(closures == null) {
			closures = new HashSet<String>();
		}
		
		closures.add(name);
		attributes.put(name, closure);
	}
	
	public void setTrait(String name, Object value) {
		if(traits == null) {
			traits = new HashSet<String>();
		}
		
		traits.add(name);
		attributes.put(name, value);
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
	
	public List<Object> traitAttributes(){
		List<Object> traitAttributes = new ArrayList<Object>(traits.size());
		for(String trait : traits) {
			traitAttributes.add(get(trait));
		}
		return traitAttributes;
	}
	
	public Namespace duplicate(){
		Namespace dup = new Namespace();
		
		dup.attributes.putAll(attributes);
		if(finalAttributes != null) {
			dup.finalAttributes = new HashSet<String>();
			dup.finalAttributes.addAll(finalAttributes);
		}
		
		if(traitSpaces != null) {
			dup.traitSpaces = new ArrayList<Namespace>(traitSpaces.size());
			dup.traitSpaces.addAll(traitSpaces);
		}
		
		return dup;
	}
}

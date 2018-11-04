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
	private List<String> traits;
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
		
		Object result = attributes.get(name);
		// we don't have this ourselves - search our traits to see if one of them
		// has the value
		if(traits != null && result == null) {
			for(int i = 0; i < traits.size(); i++) {
				Object value = ((CObject) attributes.get(traits.get(i))).getAttributes().get(name);
				if(value != null) {
					return value;
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
			traits = new ArrayList<String>(1);
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

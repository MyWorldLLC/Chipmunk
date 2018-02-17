package chipmunk;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Namespace {

	private final Map<String, Object> attributes;
	private Set<String> finalAttributes;
	
	public Namespace(){
		attributes = new HashMap<String, Object>();
		finalAttributes = new HashSet<String>();
	}
	
	public boolean has(String name){
		return attributes.containsKey(name);
	}
	
	public Object get(String name){
		return attributes.get(name);
	}
	
	public void set(String name, Object value) throws IllegalArgumentException {
		
		if(value == null){
			throw new NullPointerException("Cannot set null attributes: use CNull instead");
		}
		
		if(finalAttributes != null && finalAttributes.contains(name)){
			throw new IllegalArgumentException("Cannot set final attribute: " + name);
		}
		
		attributes.put(name, value);
	}
	
	public void setFinal(String name, Object value){
		
		if(value == null){
			throw new NullPointerException("Cannot set null attributes: use CNull instead");
		}
		
		if(finalAttributes == null){
			finalAttributes = new HashSet<String>();
		}
		
		finalAttributes.add(name);
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
	
	public Namespace duplicate(){
		Namespace dup = new Namespace();
		
		dup.attributes.putAll(attributes);
		dup.finalAttributes.addAll(finalAttributes);
		
		return dup;
	}
}

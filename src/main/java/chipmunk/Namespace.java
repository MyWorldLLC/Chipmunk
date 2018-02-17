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
	
	public Object getAttribute(String name){
		return attributes.get(name);
	}
	
	public void setAttribute(String name, Object attribute) throws IllegalArgumentException {
		
		if(attribute == null){
			throw new NullPointerException("Cannot set null attributes: use CNull instead");
		}
		
		if(finalAttributes != null && finalAttributes.contains(name)){
			throw new IllegalArgumentException("Cannot set final attribute: " + name);
		}
		
		attributes.put(name, attribute);
	}
	
	public void setFinalAttribute(String name, Object attribute){
		
		if(attribute == null){
			throw new NullPointerException("Cannot set null attributes: use CNull instead");
		}
		
		if(finalAttributes == null){
			finalAttributes = new HashSet<String>();
		}
		
		finalAttributes.add(name);
		attributes.put(name, attribute);
	}
	
	public Set<String> attributeNames(){
		return attributes.keySet();
	}
	
	public Set<String> finalAttributeNames(){
		if(finalAttributes == null){
			return null;
		}
		return Collections.unmodifiableSet(finalAttributes);
	}
}

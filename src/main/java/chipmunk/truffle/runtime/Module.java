package chipmunk.truffle.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.object.DynamicObject;

public class Module {
	
	private Map<String, Object> variables;
	private final String name;
	private final DynamicObject obj;
	
	public Module(String name) {
		this.name = name;
		obj = null; // new DynamicObject();
		variables = new HashMap<String, Object>();
	}
	
	public String getName() {
		return name;
	}
	
	public Object setVariable(String name, Object value) {
		variables.put(name, value);
		return value;
	}
	
	public Object getVariable(String name) {
		return variables.get(name);
	}
	
	public boolean hasVariable(String name) {
		return variables.containsKey(name);
	}
	
	public Collection<String> getVariables(){
		return variables.keySet();
	}

}

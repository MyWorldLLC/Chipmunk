package chipmunk;

import java.util.HashMap;
import java.util.Map;

import chipmunk.modules.lang.CObject;

public class Namespace {

	protected Map<String, CObject> variables;
	
	public Namespace(){
		variables = new HashMap<String, CObject>();
	}
	
	public CObject getObject(String name){
		
		CObject val = variables.get(name);
		
		if(val == null){
			throw new MissingVariableChipmunk("No variable with name " + name);
		}
		
		return val;
	}
	
	public void setVariable(String name, CObject object){
		
		if(object == null){
			throw new NullChipmunk("Attempt to set null variable " + name);
		}
		
		variables.put(name, object);
	}
}

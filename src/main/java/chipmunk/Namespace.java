package chipmunk;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import chipmunk.modules.lang.CObject;

public class Namespace {

	protected Map<String, CObject> variables;
	
	public Namespace(){
		variables = new HashMap<String, CObject>();
	}
	
	public CObject getObject(String name){
		return variables.get(name);
	}
	
	public void setVariable(String name, CObject object){
		
		if(object == null){
			throw new NullChipmunk("Attempt to set null variable " + name);
		}
		
		variables.put(name, object);
	}
	
	public Set<String> variableNames(){
		return variables.keySet();
	}
}

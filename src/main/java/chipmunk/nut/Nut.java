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
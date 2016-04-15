package chipmunk.nut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import chipmunk.ChipmunkContext;
import chipmunk.modules.lang.CObject;

public class Nut {
	
	protected Map<String, String> metadata;
	protected List<CObject> instances;
	protected IdentityHashMap<CObject, Integer> identityMap;
	
	public Nut(){
		
		metadata = new HashMap<String, String>();
		instances = new ArrayList<CObject>();
		identityMap = new IdentityHashMap<CObject, Integer>();
		
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
	
	public List<CObject> getInstances(){
		return instances;
	}
	
	public List<CObject> unpack(ChipmunkContext context, byte[] data){
		return null;
	}

	
}
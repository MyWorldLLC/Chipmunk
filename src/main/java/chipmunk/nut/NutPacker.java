package chipmunk.nut;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chipmunk.modules.lang.CObject;
import chipmunk.modules.lang.CType;

public class NutPacker {
	
	public static final byte NUT_VERSION = (byte) 0x10;
	
	protected IdentityHashMap<CObject, Integer> identityMap;
	protected int identityCounter;
	
	protected Set<CType> types;
	
	protected List<CObject> instances;
	protected Deque<Integer> primaries;
	
	private boolean isPackingCode;
	
	public NutPacker(){
		
		identityMap = new IdentityHashMap<CObject, Integer>();
		identityCounter = 0;
		
		types = new HashSet<CType>();
		
		instances = new ArrayList<CObject>();
		primaries = new ArrayDeque<Integer>();
		
		isPackingCode = false;
		
	}
	
	
	public boolean isPackingCode(){
		return isPackingCode;
	}
	
	public void packCode(boolean packCode){
		isPackingCode = packCode;
	}
	
	public void reset(){
		
		identityMap.clear();
		identityCounter = 0;
		
		types.clear();
		
		instances.clear();
		primaries.clear();
		
	}
	
	private int register(CObject obj, boolean primary){
		
		int identity = identityCounter;
		identityMap.put(obj, identityCounter);
		identityCounter++;
		
		instances.set(identity, obj);
		if(primary){
			primaries.add(identity);
		}
		
		types.add(obj.getType());
		return identity;
	}
	
	public int registerSecondary(CObject obj){
		return register(obj, false);
	}
	
	public int getPackIndex(CObject obj){
		
		if(identityMap.containsKey(obj)){
			return identityMap.get(obj);
		}else{
			return register(obj, false);
		}
		
	}
	
	public void pack(Nut nut, OutputStream out){
		
		List<CObject> nutInstances = nut.getInstances();
		
		// register all primaries
		for(int i = 0; i < nutInstances.size(); i++){
			CObject instance = nutInstances.get(i);
			int index = register(instance, true);
			instances.set(index, instance);
		}
		
		// take pre-pack pass over instances (allows registering secondaries)
		//  - list may grow during iteration
		for(int i = 0; i < instances.size(); i++){
			instances.get(i).__prePack__(this);
		}
		
		Map<CType, Integer> typeIndices = new HashMap<CType, Integer>();
		
		// this gets serialized into the packed type table
		List<CType> indexedTypes = new ArrayList<CType>();
		int typeIdentity = 0;
		
		for(CType type : types){
			typeIndices.put(type, typeIdentity);
			indexedTypes.add(type);
			typeIdentity++;
		}
		
		OutputCapsule capsule = new OutputCapsule(out);
		
		capsule.write(NutFormat.MAGIC_NUMBER);
		capsule.write(NUT_VERSION);
		
		// write metadata
		capsule.write(NutFormat.TABLE_MARKER);
		
		Map<String, String> metadata = nut.getMetaData();
		
		capsule.write(metadata.size());
		
		for(String s : metadata.keySet()){
			capsule.write(s);
			capsule.write(metadata.get(s));
		}
		
		// write type table. Each entry is the string serialized type name,
		// followed by the string serialized name of the module defining the type
		capsule.write(NutFormat.TABLE_MARKER);
		
		capsule.write(indexedTypes.size());
		
		for(int i = 0; i < indexedTypes.size(); i++){
			
			CType type = indexedTypes.get(i);
			capsule.write(type.getName());
			capsule.write(type.getModule().getName());
			
		}
		
		capsule.write(NutFormat.TABLE_MARKER);
		
		// write instance table. Each entry is a primary/secondary marker, followed
		// by an integer index into the type table.
		capsule.write(NutFormat.TABLE_MARKER);
		
		capsule.write(instances.size());
		
		for(int i = 0; i < instances.size(); i++){
			
			CObject instance = instances.get(i);
			
			Integer index = primaries.peek();
			if(index != null && index == i){
				capsule.write(NutFormat.PRIMARY_INSTANCE);
				primaries.pop();
			}else{
				capsule.write(NutFormat.SECONDARY_INSTANCE);
			}
			
			capsule.write(typeIndices.get(instance.getType()));
			
		}
		
		capsule.write(NutFormat.TABLE_MARKER);
		
		// pack instances into data table
		capsule.write(NutFormat.TABLE_MARKER);
		for(int i = 0; i < instances.size(); i++){
			CObject instance = instances.get(i);
			instance.__pack__(this, capsule);
		}
		
		capsule.write(NutFormat.TABLE_MARKER);
		
	}

}
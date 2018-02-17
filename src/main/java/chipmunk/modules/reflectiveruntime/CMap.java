package chipmunk.modules.reflectiveruntime;

import java.util.HashMap;
import java.util.Map;

import chipmunk.ChipmunkVM;

public class CMap implements RuntimeObject {
	
	protected Map<Object, Object> map;
	
	public CMap(){
		map = new HashMap<Object, Object>();
	}
	
	public CMap(Map<Object, Object> map){
		this.map = map;
	}
	
	public void clear(){
		map.clear();
	}
	
	public CBoolean containsKey(ChipmunkVM vm, Object key){
		vm.traceBoolean();
		return new CBoolean(map.containsKey(key));
	}
	
	public CBoolean containsValue(ChipmunkVM vm, Object value){
		vm.traceBoolean();
		return new CBoolean(map.containsValue(value));
	}
	
	public CBoolean equals(ChipmunkVM vm, Object o){
		vm.traceBoolean();
		return new CBoolean(map.equals(o));
	}
	
	public Object get(Object key){
		return map.get(key);
	}
	
	public Object getAt(ChipmunkVM vm, Object key){
		return map.get(key);
	}
	
	public Object setAt(ChipmunkVM vm, Object key, Object value){
		boolean had = map.containsKey(key);
		Object former = map.put(key, value);
		
		if(had){
			return former;
		}
		vm.traceMem(8);
		return new CNull();
	}
	
	public CInteger hashCode(ChipmunkVM vm){
		vm.traceBoolean();
		return new CInteger(map.hashCode());
	}
	
	public CBoolean isEmpty(ChipmunkVM vm){
		vm.traceBoolean();
		return new CBoolean(map.isEmpty());
	}
	
	public Object put(ChipmunkVM vm, Object key, Object value){
		return map.put(key, value);
	}
	
	public Object put(Object key, Object value){
		return map.put(key, value);
	}
	
	public void putAll(Map<? extends Object, ? extends Object> m){
		map.putAll(m);
	}
	
	public Object remove(Object key){
		return map.remove(key);
	}
	
	public CBoolean remove(ChipmunkVM vm, Object key, Object value){
		vm.traceBoolean();
		return new CBoolean(map.remove(key, value));
	}
	
	public Object replace(Object key, Object value){
		return map.replace(key, value);
	}
	
	public CBoolean replace(ChipmunkVM vm, Object key, Object oldValue, Object newValue){
		vm.traceBoolean();
		return new CBoolean(map.replace(key, oldValue, newValue));
	}
	
	public CInteger size(ChipmunkVM vm){
		vm.traceInteger();
		return new CInteger(map.size());
	}
	
	public int size(){
		return map.size();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		
		for(Map.Entry<Object, Object> entry : map.entrySet()){
			sb.append(entry.getKey().toString());
			sb.append(':');
			sb.append(entry.getValue().toString());
		}
		
		sb.append('}');
		return map.toString();
	}

}

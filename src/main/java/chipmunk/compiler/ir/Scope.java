package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class Scope {

	protected List<String> names;
	protected Scope enclosing;
	protected boolean allowOverrides;
	
	public Scope(){
		names = new ArrayList<String>();
	}
	
	public void add(String name){
		names.add(name);
	}
	
	public boolean has(String name){
		if(names.contains(name)){
			return true;
		}else{
			return false;
		}
	}
	
	public void remove(String name){
		names.remove(name);
	}
	
	public List<String> getNames(){
		return names;
	}
	
	public Scope getEnclosing(){
		return enclosing;
	}
	
	public void setEnclosing(Scope scope){
		enclosing = scope;
	}
	
	public boolean allowOverrides(){
		return allowOverrides;
	}
	
	public void setAllowOverrides(boolean allow){
		allowOverrides = allow;
	}
}

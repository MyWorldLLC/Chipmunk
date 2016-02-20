package chipmunk.modules.lang;

import java.util.ArrayList;
import java.util.List;

public class Module extends CObject {
	
	protected String name;
	protected List<Module> modules;
	
	
	public Module(){
		modules = new ArrayList<Module>();
		type = new ModuleType();
	}
	
	public Module(String name){
		this();
		this.setName(name);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	@Override
	public boolean equals(Object other){
		
		// Modules are considered equal if their names are equal
		if(other instanceof Module){
			if(((Module) other).getName().equals(name)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
		
	}
}

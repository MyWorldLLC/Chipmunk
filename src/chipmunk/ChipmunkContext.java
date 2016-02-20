package chipmunk;

import java.util.ArrayList;
import java.util.List;

import chipmunk.modules.lang.Module;

public class ChipmunkContext {

	List<Module> modules;
	
	public ChipmunkContext(){
		modules = new ArrayList<Module>();
	}
	
	public Module getModule(String name){
		for(int i = 0; i < modules.size(); i++){
			Module module = modules.get(i);
			if(module.getName().equals(name)){
				return module;
			}
		}
		return null;
	}
	
	public void addModule(Module module){
		
		for(int i = 0; i < modules.size(); i++){
			
			Module existing = modules.get(i);
			
			if(existing.equals(module)){
				modules.remove(i);
				modules.add(i, module);
				return;
			}
		}
		modules.add(module);
	}
	
	public boolean removeModule(Module module){
		
		for(int i = 0; i < modules.size(); i++){
			
			Module existing = modules.get(i);
			
			if(existing.equals(module)){
				modules.remove(i);
				return true;
			}
		}
		return false;
	}
	
}

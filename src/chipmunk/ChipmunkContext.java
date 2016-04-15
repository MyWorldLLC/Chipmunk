package chipmunk;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import chipmunk.modules.lang.CObject;
import chipmunk.modules.lang.Module;

public class ChipmunkContext {

	Map<String, Module> modules;
	ArrayDeque<CObject> stack;
	
	public ChipmunkContext(){
		modules = new HashMap<String, Module>();
		// initialize operand stack to be 128 elements deep
		stack = new ArrayDeque<CObject>(128);
	}
	
	public Module getModule(String name){
		return modules.get(name);
	}
	
	public void addModule(Module module){
		modules.put(module.getName(), module);
	}
	
	public boolean removeModule(Module module){
		
		Module removed = modules.remove(module.getName());
		
		if(removed == null){
			return false;
		}else{
			return true;
		}
		
	}
	
	public void push(CObject obj){
		stack.push(obj);
	}
	
	public CObject pop(){
		return stack.pop();
	}
	
}

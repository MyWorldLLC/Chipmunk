package chipmunk;

import java.util.ArrayList;
import java.util.List;

import chipmunk.modules.lang.CObject;

public class Namespace {

	protected List<Variable> variables;
	
	public Namespace(){
		variables = new ArrayList<Variable>();
	}
	
	public CObject getObject(int index){
		try{
			return variables.get(index).getObject();
		}catch(IndexOutOfBoundsException e){
			throw new AngryChipmunk("Variable index out of range");
		}
		
	}
	
	public CObject getObject(String name){
		
		for(int i = 0; i < variables.size(); i++){
			Variable var = variables.get(i);
			if(var.getName().equals(name)){
				return var.getObject();
			}
		}
		throw new AngryChipmunk("No variable with name " + name);
	}
	
	public void setVariable(String name, CObject object){
		for(int i = 0; i < variables.size(); i++){
			
			Variable var = variables.get(i);
			
			// if we already have a variable with this name,
			// overwrite it and return
			if(var.getName().equals(name)){
				
				var.setObject(object);
				return;
			}
		}
		
		// this variable doesn't yet exist - create it
		variables.add(new Variable(name, object));
	}
}

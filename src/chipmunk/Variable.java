package chipmunk;

import chipmunk.modules.lang.CObject;

public class Variable {

	protected String name;
	protected CObject object;
	
	public Variable(){}
	
	public Variable(String varName, CObject value){
		name = varName;
		object = value;
	}
	
	public String getName(){
		return name;
	}
	
	public CObject getObject(){
		return object;
	}
	
	public void setObject(CObject object){
		this.object = object;
	}
	
}

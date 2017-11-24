package chipmunk.modules.reflectiveruntime;

import chipmunk.reflectors.ContextOperator;

public class CBoolean implements ContextOperator {
	
	private final boolean value;
	
	public CBoolean(boolean value){
		this.value = value;
	}
	
	public boolean getValue(){
		return value;
	}
	
	public boolean booleanValue(){
		return value;
	}

}

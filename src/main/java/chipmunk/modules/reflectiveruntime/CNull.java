package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;

public class CNull implements RuntimeObject {

	public CBoolean truth(ChipmunkVM context){
		context.traceMem(1);
		return new CBoolean(false);
	}
	
	public String toString(){
		return "CNull";
	}
	
	public boolean equals(Object other){
		if(other instanceof CNull){
			return true;
		}
		return false;
	}
}

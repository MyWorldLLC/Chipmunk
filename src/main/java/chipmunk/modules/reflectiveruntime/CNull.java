package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;

public class CNull implements RuntimeObject {
	
	private static CNull instance;
	
	private CNull() {}
	
	public static CNull instance() {
		if(instance == null) {
			instance = new CNull();
		}
		return instance;
	}

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

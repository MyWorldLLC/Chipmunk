package chipmunk.modules.runtime;

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

	public CBoolean equals(ChipmunkVM vm, Object other){
		vm.traceBoolean();
		return new CBoolean(equals(other));
	}
	
	public boolean equals(Object other){
		if(other instanceof CNull){
			return true;
		}
		return false;
	}
}

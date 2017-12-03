package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;
import chipmunk.reflectors.VMOperator;

public class CNull implements VMOperator {

	public CBoolean truth(ChipmunkVM context){
		context.traceMem(1);
		return new CBoolean(false);
	}
	
	public String toString(){
		return "CNull";
	}
}

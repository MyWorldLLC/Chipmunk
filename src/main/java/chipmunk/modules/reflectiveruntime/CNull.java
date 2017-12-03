package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkContext;
import chipmunk.reflectors.ContextOperator;

public class CNull implements ContextOperator {

	public CBoolean truth(ChipmunkContext context){
		context.traceMem(1);
		return new CBoolean(false);
	}
	
	public String toString(){
		return "CNull";
	}
}

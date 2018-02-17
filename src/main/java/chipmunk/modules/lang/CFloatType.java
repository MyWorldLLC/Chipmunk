package chipmunk.modules.lang;

import chipmunk.ChipmunkVM;

public class CFloatType extends CType {

	protected CFloat tempValue;
	
	public CFloatType(){
		super("Float");
		tempValue = new CFloat();
		tempValue.type = this;
		tempValue.namespace.set("type", this);
	}
	
	public CFloat getTemp(){
		return tempValue;
	}
	
	public CObject instance(){
		return new CFloat();
	}
	
	/*@Override
	public CObject __call__(ChipmunkContext context, int params, boolean resuming){
		if(params == 0){
			return new CFloat();
		}else if(params == 1){
			return new CFloat(((CFloat)(context.pop().__as__(this))).getValue());
		}else{
			throw new UnimplementedOperationChipmunk("CFloatType.__call__() is not defined for parameter count: " + params);
		}
	}*/
}

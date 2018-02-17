package chipmunk.modules.lang;

import chipmunk.ChipmunkVM;

public class CIntType extends CType {

	protected CInt tempValue;
	
	public CIntType(){
		super("Int");
		tempValue = new CInt();
		tempValue.type = this;
		tempValue.namespace.set("type", this);
	}
	
	public CInt getTemp(){
		return tempValue;
	}
	
	public CObject instance(){
		return new CInt();
	}
	
	/*@Override
	public CObject __call__(ChipmunkContext context, int params, boolean resuming){
		if(params == 0){
			return new CInt();
		}else if(params == 1){
			return new CInt(((CInt)(context.pop().__as__(this))).getValue());
		}else{
			throw new UnimplementedOperationChipmunk("CIntType.__call__() is not defined for parameter count: " + params);
		}
	}*/
}

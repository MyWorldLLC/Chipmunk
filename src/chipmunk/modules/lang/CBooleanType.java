package chipmunk.modules.lang;

import chipmunk.ChipmunkContext;

public class CBooleanType extends CType {
	
	protected CBoolean tempValue;
	
	public CBooleanType(){
		super("Boolean");
		tempValue = new CBoolean();
		tempValue.type = this;
		tempValue.namespace.setVariable("type", this);
	}
	
	public CBoolean getTemp(){
		return tempValue;
	}
	
	@Override
	public CObject __call__(ChipmunkContext context, int params, boolean resuming){
		if(params == 0){
			return new CBoolean();
		}else if(params == 1){
			return new CBoolean(((CBoolean)(context.pop().__as__(this))).getValue());
		}else{
			throw new UnimplementedOperationChipmunk("CBooleanType.__call__() is not defined for parameter count: " + params);
		}
	}

}

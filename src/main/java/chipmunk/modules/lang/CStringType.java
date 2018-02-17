package chipmunk.modules.lang;

import chipmunk.ChipmunkVM;

public class CStringType extends CType {
	
	protected CString tempString;
	
	public CStringType(){
		super("String");
		tempString = new CString();
		tempString.type = this;
		tempString.namespace.set("type", this);
	}
	
	public CString getTemp(){
		return tempString;
	}
	
	public CObject instance(){
		return new CString();
	}
	
	/*
	@Override
	public CObject __call__(ChipmunkContext context, int params, boolean resuming){
		if(params == 0){
			return new CString();
		}else if(params == 1){
			return new CString(((CString)(context.pop().__as__(this))).getValue());
		}else{
			throw new UnimplementedOperationChipmunk("CStringType.__call__() is not defined for parameter count: " + params);
		}
	}*/

}
